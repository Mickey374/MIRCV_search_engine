package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class PostingList {
    /**
     * Term of the posting list
     */
    private String term;

    /**
     * List of postings
     */
    private final ArrayList<Posting> postings = new ArrayList<>();

    /**
     * the list of the blocks n which the posting list is divided
     */
    private ArrayList<BlockDescriptor> blocks = null;

    /**
     * iterator for the postings
     */
    private Iterator<Posting> postingIterator = null;

    /**
     * iterator for the blocks
     */
    private Iterator<BlockDescriptor> blocksIterator = null;

    /**
     * the current block
     */
    private BlockDescriptor currentBlock = null;

    /**
     * the current posting
     */
    private Posting currentPosting = null;

    /**
     * variable used for computing the max doc length to insert in the vocabulary to compute the BM25 term upper bound
     */
    private int BM25Dl = 1;

    /**
     * The term frequency for the upper bound for BM25
     */
    private int BM25tf = 0;

    /**
     * constructor that create a posting list from a string
     *
     * @param toParse the string from which we can parse the posting list, with 2 formats:
     *                <ul>
     *                <li>[term] -> only the posting term</li>
     *                <li>[term] \t [docid]:[frequency] [docid]:{frequency] ... -> the term and the posting list}</li>
     *                </ul>
     */
    public PostingList(String toParse) {
        String[] termRow = toParse.split("\t");
        this.term = termRow[0];
        if (termRow.length > 1) parsePostings(termRow[1]);
    }

    /**
     * Default constructor
     */
    public PostingList() {
    }

    /**
     * Parses the posting list from a string
     *
     * @param rawPostings the string to parse
     */
    private void parsePostings(String rawPostings) {
        String[] documents = rawPostings.split(" ");
        for (String elem : documents) {
            String[] posting = elem.split(":");
            postings.add(new Posting(Integer.parseInt(posting[0]), Integer.parseInt(posting[1])));
        }
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public ArrayList<Posting> getPostings() {
        return postings;
    }

    public void appendPostings(ArrayList<Posting> newPostings) {
        postings.addAll(newPostings);
    }

    /**
     * Update the max document length
     *
     * @param length the document length
     * @param tf     the term frequency
     */
    public void updateBM25Params(int length, int tf) {
        double currentRatio = (double) this.BM25tf / (double) (this.BM25Dl + this.BM25tf);
        double newRatio = (double) tf / (double) (length + tf);
        if (newRatio > currentRatio) {
            this.BM25Dl = length;
            this.BM25tf = tf;
        }
    }

    /**
     * method that opens and initializes the posting list for the query processing
     */
    public void openPostingList() {
        // load the block descriptors
        blocks = Vocabulary.getInstance().get(term).readBlocks();

        // return false if the blocks are not loaded
        if (blocks == null) {
            return;
        }

        // initialize the blocks iterator
        blocksIterator = blocks.iterator();

        // Initialize postings iterator
        postingIterator = postings.iterator();
    }

    /**
     * returns the next posting in the list
     *
     * @return the next posting in the list
     */
    public Posting next() {
        // no postings in memory: load new block
        if (!postingIterator.hasNext()) {

            // no new blocks: end of list
            if (!blocksIterator.hasNext()) {
                currentPosting = null;
                return null;
            }

            // load the new block and update the postings iterator
            currentBlock = blocksIterator.next();
            //remove previous postings
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings());
            postingIterator = postings.iterator();
        }
        // return the next posting to process
        currentPosting = postingIterator.next();
        return currentPosting;
    }

    /**
     * Returns last accessed posting
     *
     * @return posting or null if there are no more postings.
     */
    public Posting getCurrentPosting() {
        return currentPosting;
    }

    /**
     * returns the first posting with docid greater or equal than the specified docid.
     * If there's no greater or equal docid in the list returns null
     *
     * @param docid the docid to reach in the list
     * @return the first posting with docid greater or equal than the specified docid, null if this posting doesn't exist
     */
    public Posting nextGEQ(int docid) {
        // flag to check if block has changed
        boolean blockChanged = false;

        // move to the block with max docid >= docid
        // current block is null only if it's the first read
        while (currentBlock == null || currentBlock.getMaxDocid() < docid) {
            // no blocks left
            if (!blocksIterator.hasNext()) {
                currentPosting = null;
                return null;
            }

            // load the new block and update the postings iterator
            currentBlock = blocksIterator.next();
            blockChanged = true;
        }

        // Block changed, load postings and update iterator
        if (blockChanged) {
            //remove previous postings
            postings.clear();
            postings.addAll(currentBlock.getBlockPostings());
            postingIterator = postings.iterator();
        }

        // move to the first GE posting and return it
        while (postingIterator.hasNext()) {
            currentPosting = postingIterator.next();
            if (currentPosting.getDocid() >= docid) return currentPosting;
        }
        currentPosting = null;
        return null;
    }

    /**
     * Closes the list and clears all structures removing them from the vocabulary
     */
    public void closePostingList() {
        // clear the structures
        postings.clear();
        blocks.clear();
        Vocabulary.getInstance().remove(term);
    }

    /**
     * function to write the posting list as plain text in the debug files
     *
     * @param docidsPath:          path of docids file where to write
     * @param freqsPath:           path of freqs file where to write
     * @param maxPostingsPerBlock: maximum number of postings per block
     */
    public void debugSaveToDisk(String docidsPath, String freqsPath, int maxPostingsPerBlock) {
        FileUtils.createDirectory("data/debug");
        FileUtils.createIfNotExists("data/debug/" + docidsPath);
        FileUtils.createIfNotExists("data/debug/" + freqsPath);
        FileUtils.createIfNotExists("data/debug/completeList.txt");

        try {
            BufferedWriter writerDocids = new BufferedWriter(new FileWriter("data/debug/" + docidsPath, true));
            BufferedWriter writerFreqs = new BufferedWriter(new FileWriter("data/debug/" + freqsPath, true));
            BufferedWriter writerComplete = new BufferedWriter(new FileWriter("data/debug/completeList.txt", true));

            String[] postingInfo = toStringPosting();
            int postingsPerBlock = 0;

            for (Posting p : postings) {
                writerDocids.write(p.getDocid() + " ");
                writerFreqs.write(p.getFrequency() + " ");
                postingsPerBlock++;

                // Check if max number of terms per block is reached
                if (postingsPerBlock == maxPostingsPerBlock) {
                    writerDocids.write("| ");
                    writerFreqs.write("| ");
                    postingsPerBlock = 0;
                }
            }
            writerDocids.write("\n");
            writerFreqs.write("\n");

            writerComplete.write(postingInfo[0] + "\n");
            writerComplete.write(postingInfo[1] + "\n");
            writerComplete.write(this.toString());

            writerDocids.close();
            writerFreqs.close();
            writerComplete.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * prints the posting list showing docids and frequencies in different strings
     *
     * @return an array containing the docid and frequency posting list output
     */
    public String[] toStringPosting() {
        StringBuilder resultDocids = new StringBuilder();
        StringBuilder resultFreqs = new StringBuilder();

        resultDocids.append(term).append(" -> ");
        resultFreqs.append(term).append(" -> ");

        int curBlock = 0;
        int curPosting = 0;
        int numPostings = postings.size();
        int numBlocks = 1;

        if (postings.size() > 1024) {
            numBlocks = (int) Math.ceil(Math.sqrt(postings.size()));
            numPostings = (int) Math.ceil(postings.size() / (double) numBlocks);
        }

        while (curBlock < numBlocks) {
            //The number of postings in the last block may be greater from the actual number of postings it contains
            int n = Math.min(numPostings, postings.size() - curPosting);

            // Append the block info
            for (int i = 0; i < n; i++) {
                Posting p = postings.get(curPosting);
                resultDocids.append(p.getDocid());
                resultFreqs.append(p.getFrequency());

                if (i != n - 1) {
                    resultDocids.append(", ");
                    resultFreqs.append(", ");
                }
                curPosting++;
            }
            curBlock++;

            //there are iterations left
            if (curBlock != numBlocks) {
                resultDocids.append(" | ");
                resultFreqs.append(" | ");
            }
        }
        return new String[]{resultDocids.toString(), resultFreqs.toString()};
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\"");
        result.append(term);
        result.append('\t');
        for (Posting p : postings) {
            result.append(p.getDocid()).append(":").append(p.getFrequency()).append(" ");
        }
        result.append("\"");
        result.append('\n');

        return result.toString();
    }

    public int getBM25Dl() {
        return BM25Dl;
    }

    public int getBM25tf() {
        return BM25tf;
    }

    public void setBM25Dl(int BM25Dl) {
        this.BM25Dl = BM25Dl;
    }

    public void setBM25tf(int BM25tf) {
        this.BM25tf = BM25tf;
    }
}