package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Spimi {
    // Path to the file on Disk storing processed collections
    private static final String PATH_TO_DOCUMENTS = ConfigurationParams.getProcessedCollectionPath();

    // Path to the file on the Disk for storing partial indexes
    private static final String PATH_BASE_TO_INDEX = ConfigurationParams.getPartialIndexPath();

    // Path to the file on Disk for storing document index
    private static final String PATH_TO_DOCUMENT_INDEX = ConfigurationParams.getDocumentIndexPath();

    // Memory alloc to be kept free
    private static final long MEMORY_THRESHOLD = 106099200;

    // Hashmap to store the partial Inverted index
    private static HashMap<String, ArrayList<MutablePair<Integer, Integer>>> index = new HashMap<>();

    // Counter for the partial indexes created
    private static int num_index = 0;

    /**
     * Function to flush partial index onto disk
     * and cleans the index data structure so it can be re-used
     *
     * @param path
     */
    private static void save_index_to_disk(String path) {
        System.out.println("**** Writing file to: " + path + " ****");

        // If index is empty, then there is nothing to write.
        if (index.isEmpty())
            return;

        // Create the new file to save the index
        FileUtils.CreateOrCleanFile(path);

        // Create the new file entry
        StringBuilder entry = new StringBuilder();

        // Sort the index in lexicographic order
        index = index.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        ArrayList<MutablePair<Integer, Integer>> postingList;
        // For each of the Posting lists in the Inverted Index
        for(String key: index.keySet()) {
            // Initialize the entry to write on file
            entry.append(key).append("\t");

            // get the PostingLists from index
            postingList = index.get(key);

            // Sort in ascending order the PostingLists by docId
            postingList.sort(Comparator.comparing(MutablePair::getLeft));

            /* Create the entry in this format for each term
                term \t docid1,freq1 docid2,freq2 ... docidN,freqN \n
             */
            for(int i = 0; i < postingList.size() -1; i++) {
                entry.append(postingList.get(i).getLeft()).append(",").append(postingList.get(i).getRight()).append(" ");
            }

            // Append the last posting
            entry.append(postingList.get(postingList.size() -1).getLeft()).append(",").append(postingList.get(postingList.size() - 1).getRight()).append("\n");
            writeEntry(path, entry.toString());         // Write entry on file
            entry.setLength(0);     // Clear entry to start over with the new term
        }

        index.clear();      // empty index
        num_index++;        // update the number of partial inverted indexes
    }

    /**
     * Utility function writing an entry on disk.
     * @param path: path to file on disk
     * @param entry: entry to write on disk
     */
    private static void writeEntry(String path, String entry){
        try {
            Files.writeString(Paths.get(path), entry, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Function that searches for a given docid in a posting list
     * If the document already exists, updates the frequency for that document
     * Else it creates a new pair of (docid, freq) with freq = 1 and added to postingList
     * @param docid: docid of a document
     * @param postingList: postingList of a given term
     */
    private static void updateOrAddPosting(int docid, ArrayList<MutablePair<Integer, Integer>> postingList){
        boolean found = false;
        // Iterate on each postingList
        for(MutablePair<Integer, Integer> posting: postingList){    // docid found
            if(docid == posting.getLeft()){
                posting.setRight(posting.getRight() + 1);       //update frequency +1
                found = true;
            }
        }
        if(!found)          // document with the docid was not in posting list
            postingList.add(new MutablePair<>(docid, 1));       //create new pair and add it

    }


    /**
     * Returns the posting list of a given term
     * @param term: a string describing a word
     * @return the posting list of the given term
     */
    private static ArrayList<MutablePair<Integer, Integer>> getPostingList(String term){
        return index.get(term);
    }

    /**
     * Creates the posting list of the given term
     * @param term a string describing a word token
     * @return
     */
    private static ArrayList<MutablePair<Integer, Integer>> createPostingList(String term){
        ArrayList<MutablePair<Integer, Integer>> postingList = new ArrayList<>();   // create posting list
        index.put(term, postingList);       // add entry to index
        return postingList;
    }


    public static void spimi(){
        // First ensure the document index is created or cleaned
        FileUtils.CreateOrCleanFile(PATH_TO_DOCUMENT_INDEX);

        // Use the BufferedReader to read processed docs line by line
        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_TO_DOCUMENTS))) {
            int pid;            // doc number of document
            String[] terms;     // terms contained in the document
            String[] document; // document[0] -> pid document[1] -> terms

            String line;        // line read from disk
            ArrayList<MutablePair<Integer, Integer>> posting;       // posting list of a given term
            boolean allDocumentsProcessed = false;

            int docid = 0;      // count for doc ids

            while(!allDocumentsProcessed) {
                // Parse each line to extract the docid and terms

                while(Runtime.getRuntime().freeMemory() > MEMORY_THRESHOLD){
                    // Build the index until memory is available with memory threshold
                    System.out.println(Runtime.getRuntime().freeMemory());

                    line = br.readLine();
                    if(line == null){           // If all documents are processed
                        allDocumentsProcessed = true;
                        break;
                    }

                    // Writes an entry to the document index file to get pid and all terms of a doc
                    document = line.split("\t");
                    pid = Integer.parseInt(document[0]);
                    if(document.length > 1) {
                        terms = document[1].split(",");

                        // Write to index and save unto disk in format docid \t pid, document_length \n
                        String entry = docid++ + "\t" + pid + "," + terms.length + "\n";

                        writeEntry(PATH_TO_DOCUMENT_INDEX, entry);

                        // For each term, updates and creates a posting list in the index
                        for(String term : terms) {
                            if(!index.containsKey(term)) {
                                // Create new posting list if the term was not present
                                posting = createPostingList(term);
                            } else {
                                // Term is present, so get its posting list
                                posting = getPostingList(term);
                            }

                            // Insert or update the new posting
                            updateOrAddPosting(docid, posting);
                        }
                    }
                }

                // If the available memory falls below the threshold or all docs processed,
                // Flash the current in-memory index to disk
                save_index_to_disk(PATH_BASE_TO_INDEX + num_index + ".txt");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}