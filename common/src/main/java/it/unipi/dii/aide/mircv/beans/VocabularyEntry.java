package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import it.unipi.dii.aide.mircv.config.CollectionSize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Entry of the vocabulary for a term
 */
public class VocabularyEntry {
    /**
     * Path to the block descriptor file
     */
    private static String BLOCK_DESCRIPTORS_PATH = ConfigurationParams.getBlockDescriptorsPath();

    /**
     * The term
     */
    private String term;

    /**
     * The document frequency of the term
     */
    private int df = 0;

    /**
     * The inverse document frequency of the term
     */
    private double idf = 0;

    /**
     * The maximum frequency of the term in a document
     */
    private int maxTf = 0;

    /**
     * The Document length used to compute BM25 term upper bound
     */
    private int BM25Dl = 1;

    /**
     * The term frequency used to compute the BM25 term upper bound
     */
    private int BM25Tf = 0;

    /**
     * The max value of TFIDF for the term
     */
    private double maxTFIDF = 0;

    /**
     * The max value of BM25 for the term
     */
    private double maxBM25 = 0;

    /**
     * Getter for BM25Dl
     */
    public int getBM25Dl() {
        return BM25Dl;
    }

    /**
     * Setter for BM25Dl
     */
    public void setBM25Dl(int BM25Dl) {
        this.BM25Dl = BM25Dl;
    }

    /**
     * Getter for BM25Tf
     */
    public int getBM25Tf() {
        return BM25Tf;
    }

    /**
     * Setter for BM25Tf
     */
    public void setBM25Tf(int BM25Tf) {
        this.BM25Tf = BM25Tf;
    }

    /**
     * method to update the max document length for the term
     * @param tf the new term frequency to process
     * @param dl the new document length to process
     */
    public void updateBM25Statistics(int tf, int dl) {
        double currentRatio = (double) this.BM25Tf / (double) (this.BM25Dl + this.BM25Tf);
        double newRatio = (double) tf / (double) (dl + tf);

        if (newRatio > currentRatio) {
            this.BM25Dl = dl;
            this.BM25Tf = tf;
        }
    }

    /**
     * starting point of the term's posting list in the inverted index in bytes
     */
    private long docidOffset = 0;

    /**
     * Starting point of the frequencies in the inverted index in bytes
     */
    private long frequencyOffset = 0;


    /**
     * size of the term's posting list in the docid file of the inverted index in bytes
     */
    private int docidSize = 0;

    /**
     * size of the term's posting list in the frequency file of the inverted index in bytes
     */
    private int frequencySize = 0;

    /**
     * number of blocks in which the posting list is divided; 1 is the default value (all the postings in the same block)
     */
    private int numBlocks = 1;

    /**
     * start offset of the block descriptors in the block descriptor file
     */
    private long blockOffset = 0;

    /**
     * size of the term; if a term is greater than this size it'll be truncated
     */
    public static final int TERM_SIZE = 64;

    /**
     * we have to store the term size plus 7 ints, 3 double and 3 longs, total 136 bytes
     */
    public static final long ENTRY_SIZE = TERM_SIZE + 76;

    /**
     * Constructor for the vocabulary entry
     * create an empty class
     */
    public VocabularyEntry() {}

    /**
     * Constructor for the vocabulary entry
     * @param term the term
     */
    public VocabularyEntry(String term) {
        this.term = term;
    }

    /**
     * updates the statistics of the vocabulary:
     * updates the max tf and df with the data of the partial posting list processed
     * @param list the posting list from which the method computes the statistics
     */
    public void updateStatistics(PostingList list) {
        // For each element of the intermediate posting list
        for(Posting posting : list.getPostings()) {

            // Update the maximum term frequency
            if(posting.getFrequency() > this.maxTf)
                this.maxTf = posting.getFrequency();

            // Update the document frequency
            this.df++;
        }
    }

    /**
     * Compute the idf using the values computed during the merging of the indexes
     */
    public void computeIdf() {
        this.idf = Math.log((double) CollectionSize.getCollectionSize() / (double) this.df);
    }

    /**
     * Setter for the docsize
     */
    public void setDocidSize(int docidSize) {
        this.docidSize = docidSize;
    }

    /**
     * Setter for the frequency size
     */
    public void setFrequencySize(int frequencySize) {
        this.frequencySize = frequencySize;
    }

    /**
     * Memory Offset of the vocabulary entry
     */
    public void setMemoryOffset(long memoryOffset) {
        this.docidOffset = memoryOffset;
    }

    /**
     * Setter for Frequency Offset
     */
    public void setFrequencyOffset(long frequencyOffset) {
        this.frequencyOffset = frequencyOffset;
    }

    /**
     * Setter for the doc frequency
     */
    public void setDf(int df) {
        this.df = df;
    }

    /**
     * Write the vocabulary entry to the vocabulary file
     * @param fChan    : fileChannel of the vocabulary file
     * @param position : position to start writing from
     * @return offset representing the position of the last written byte
     */
    public long writeEntryToDisk(long position, FileChannel fChan) {
        try {
            // Create a buffer for the term
            MappedByteBuffer buffer = fChan.map(FileChannel.MapMode.READ_WRITE, position, ENTRY_SIZE);

            // Buffer not created
            if (buffer == null)
                return -1;

            // allocate char buffer to write term
            CharBuffer charBuffer = CharBuffer.allocate(TERM_SIZE);

            // Populate the buffer with the term
            for(int i = 0; i < term.length(); i++)
                charBuffer.put(i, term.charAt(i));

            // Write the term to the buffer
            buffer.put(StandardCharsets.UTF_8.encode(charBuffer));

            // Write the document frequency
            buffer.putInt(df);
            buffer.putDouble(idf);

            // Write the max term frequency
            buffer.putInt(maxTf);
            buffer.putInt(BM25Dl);
            buffer.putInt(BM25Tf);
            buffer.putDouble(maxBM25);
            buffer.putDouble(maxTFIDF);

            // Write the memory information
            buffer.putLong(docidOffset);
            buffer.putLong(frequencyOffset);
            buffer.putInt(docidSize);
            buffer.putInt(frequencySize);

            // Write the number of blocks
            buffer.putInt(numBlocks);
            buffer.putLong(blockOffset);

            // Return the position of the last byte written
            return position + ENTRY_SIZE;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Read the document index entry from disk
     * @param memoryOffset the memory offset from which we start reading
     * @param PATH         path of the file on disk
     * @return the position of the last byte read
     */
    public long readFromDisk(long memoryOffset, String PATH) {
        // Read the entry from disk using a FileChannel
        try (FileChannel fc = (FileChannel) Files.newByteChannel(
                Paths.get(PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {
            // Instantiate the MappedByte buffer for the entry
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, ENTRY_SIZE);

            // If buffer is not created
            if (buffer == null)
                return -1;

            // Read the term from the buffer
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);

            String[] encodedTerm = charBuffer.toString().split("\0");

            if(encodedTerm.length == 0)
                return 0;

            this.term = encodedTerm[0];

            //Instantiate the buffer for reading other information
            buffer = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset + TERM_SIZE, ENTRY_SIZE - TERM_SIZE);

            // If buffer is not created
            if (buffer == null)
                return -1;

            // Read the document frequency
            df = buffer.getInt();
            idf = buffer.getDouble();

            // Read the max term frequency
            maxTf = buffer.getInt();
            BM25Dl = buffer.getInt();
            BM25Tf = buffer.getInt();
            maxBM25 = buffer.getDouble();
            maxTFIDF = buffer.getDouble();

            // Read the memory information
            docidOffset = buffer.getLong();
            frequencyOffset = buffer.getLong();
            docidSize = buffer.getInt();
            frequencySize = buffer.getInt();

            // Read the number of blocks
            numBlocks = buffer.getInt();
            blockOffset = buffer.getLong();

            // Return the position of the last byte read
            return memoryOffset + ENTRY_SIZE;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * method used to compute the max TFIDF and BM25 used as term upper bounds
     */
    public void computeUpperBounds() {
        // Compute the term upper bound for the TFIDF
        this.maxTFIDF = (1 + Math.log10(this.maxTf)) * this.idf;

        double k1 = 1.5;
        double b = 0.75;
        double avgDocLen = (double) CollectionSize.getTotalDocLen() / (double) CollectionSize.getCollectionSize();

        this.maxBM25 = (idf * BM25Tf) / (BM25Tf + k1 * ((1 - b) + b * (double) BM25Dl / avgDocLen));
    }

    /**
     * method that computes the number of blocks of postings in which the posting list will be divided.
     * If the number of postings is < 1024 the posting list is stored in a single block.
     */
    public void computeBlocksInformation() {
        if (docidSize < 1024)
            this.numBlocks = 1;
        else
            this.numBlocks = (int) Math.ceil(Math.sqrt(df));
    }

    /**
     * computes the max number of postings that we can store in a block
     * @return the max number of postings in a block
     */
    public int getMaxNumberOfPostingsInBlock() {
        return (int) Math.ceil((double) df / (double) numBlocks);
    }

    /**
     * Getter for the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * Setter for the term
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Getter for the document frequency
     */
    public int getDf() {
        return df;
    }

    /**
     * Getter for the inverse document frequency
     */
    public double getIdf() {
        return idf;
    }

    /**
     * Getter for the Docid offset
     */
    public long getDocidOffset() {
        return docidOffset;
    }

    /**
     * Getter for the frequency offset
     */
    public long getFrequencyOffset() {
        return frequencyOffset;
    }

    /**
     * Getter for the docid size
     */
    public int getDocidSize() {
        return docidSize;
    }

    /**
     * Getter for the frequency size
     */
    public int getFrequencySize() {
        return frequencySize;
    }

    /**
     * Setter for idf
     */
    public void setIdf(double idf) {
        this.idf = idf;
    }

    /**
     * Setter for the max term frequency
     */
    public void setMaxTf(int maxTf) {
        this.maxTf = maxTf;
    }

    /**
     * Getter for the max TFIDF
     */
    public double getMaxTFIDF() {
        return maxTFIDF;
    }

    /**
     * Setter for the max TFIDF
     */
    public void setMaxTFIDF(double maxTFIDF) {
        this.maxTFIDF = maxTFIDF;
    }

    /**
     * Getter for the max BM25
     */
    public double getMaxBM25() {
        return maxBM25;
    }

    /**
     * Setter for the max BM25
     */
    public void setMaxBM25(double maxBM25) {
        this.maxBM25 = maxBM25;
    }

    /**
     * Getter for the number of blocks
     */
    public int getNumBlocks() {
        return numBlocks;
    }

    /**
     * Setter for the number of blocks
     */
    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    /**
     * Getter for the block offset
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * Setter for the block offset
     */
    public void setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
    }

    /**
     * function to write a summarization of the most important data about a vocabulary entry as plain text in the debug file
     * @param path: path of the file where to write
     */
    public void debugSaveToDisk(String path) {
        FileUtils.createDirectory("data/debug");
        FileUtils.createIfNotExists("data/debug/"+path);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/debug/"+path, true));
            writer.write(this+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to read from memory the block descriptors for the term
     * @return the arrayList of the block descriptors
     */
    public ArrayList<BlockDescriptor> readBlocks() {
        try (FileChannel fc = (FileChannel) Files.newByteChannel(
                Paths.get(BLOCK_DESCRIPTORS_PATH),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {
            ArrayList<BlockDescriptor> blocks = new ArrayList<>();

            // Instantiate the MappedByte buffer for the entry
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, blockOffset, (long) numBlocks * BlockDescriptor.BLOCK_DESCRIPTOR_ENTRY_BYTES);

            // If buffer is not created
            if (buffer == null)
                return null;

            // Read the block descriptors
            for (int i = 0; i < numBlocks; i++) {
                BlockDescriptor block = new BlockDescriptor();
                block.setDocidOffset(buffer.getLong());
                block.setFreqOffset(buffer.getLong());
                block.setDocidSize(buffer.getInt());
                block.setFreqSize(buffer.getInt());
                block.setMaxDocid(buffer.getInt());
                block.setNumPostings(buffer.getInt());
                blocks.add(block);
            }
            return blocks;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "VocabularyEntry{" +
                "term='" + term + '\'' +
                ", df=" + df +
                ", idf=" + idf +
                ", maxTf=" + maxTf +
                ", BM25Dl=" + BM25Dl +
                ", BM25Tf=" + BM25Tf +
                ", maxTFIDF=" + maxTFIDF +
                ", maxBM25=" + maxBM25 +
                ", docidOffset=" + docidOffset +
                ", frequencyOffset=" + frequencyOffset +
                ", docidSize=" + docidSize +
                ", frequencySize=" + frequencySize +
                ", numBlocks=" + numBlocks +
                ", blockOffset=" + blockOffset +
                '}';
    }

    static double truncate(double value) {
        // Use the pow() method
        double newValue = value * Math.pow(10, 4);
        newValue = Math.floor(newValue);
        newValue = newValue / Math.pow(10, 4);
        System.out.println("Truncated value: " + newValue);
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VocabularyEntry that = (VocabularyEntry) o;
        return df == that.df && Double.compare(truncate(that.idf), truncate(idf)) == 0 && maxTf == that.maxTf &&
                BM25Dl == that.BM25Dl && BM25Tf == that.BM25Tf && Double.compare(truncate(that.maxTFIDF),
                truncate(maxTFIDF)) == 0 && Double.compare(truncate(that.maxBM25), truncate(maxBM25)) == 0 &&
                docidOffset == that.docidOffset && frequencyOffset == that.frequencyOffset &&
                docidSize == that.docidSize && frequencySize == that.frequencySize && numBlocks == that.numBlocks &&
                blockOffset == that.blockOffset && Objects.equals(term, that.term);
    }

    /**
     * For testing purposes
     * @param path the test path for the block descriptors
     */
    public static void setBlockDescriptorsPath(String path) {
        BLOCK_DESCRIPTORS_PATH = path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, df, idf, maxTf, BM25Dl, BM25Tf, maxTFIDF, maxBM25, docidOffset, frequencyOffset, docidSize, frequencySize, numBlocks, blockOffset);
    }
}
