package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.VocabularyEntry;
import it.unipi.dii.aide.mircv.utils.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class implementing the Single Pass In Memory Indexing algorithm for Intermediate Posting Lists
 */
public class Merger {
    /**
     * List of BufferReaders for each intermediate index.
     * If the value is null, it means index has been processed already
     */
    private static final ArrayList<BufferedReader> buffers = new ArrayList<>();

    /**
     * Contains the list of next terms to process in a lexicographic order for intermediate index
     * If the value is null, it means index has been processed already
     */
    private static final ArrayList<String> nextTerm = new ArrayList<>();

    /**
     * Contains list of next posting list to process for intermediate indexes
     * If the value is set to null, the index has been fully processed
     */
    private static final ArrayList<PostingList> nextPostingList = new ArrayList<>();

    /**
     * Standard pathname for intermediate index files.
     */
    private static final String INTERMEDIATE_INDEX_PATH = "data/index/index_";

    /**
     * Num of Open Indexes to process: When 0, means all indexes are processed
     */
    private static int openIndexes;

    private static final ArrayList<VocabularyEntry> vocabulary = new ArrayList<>();

    /**
     * Method that initializes data structures and opens buffers
     * and initializing the lists pointing to first term to process
     * in each index.
     * @throws Exception related to buffer opening and handling.
     */
    private static void initialize() throws Exception{
        openIndexes = Utility.getNumIndexes();

        // number of empty buffers
        int emptyIndexes = 0;

        for(int i=0; i< openIndexes; i++){
            String path = INTERMEDIATE_INDEX_PATH + i + ".txt";

            BufferedReader buffer = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);
            String line = buffer.readLine();

            // If buffer is empty, then add null to its position
            if(line == null) {
                buffers.add(null);
                nextTerm.add(null);
                nextPostingList.add(null);

                // Increase the number of empty indexes
                emptyIndexes++;
                continue;
            }

            PostingList list = new PostingList(line);

            buffers.add(buffer);
            nextTerm.add(list.getTerm());
            nextPostingList.add(list);
        }

        // Fix the number of open buffers by removing the null buffers
        openIndexes -= emptyIndexes;
    }

    /**
     * Read a new line from a buffer and update the data structures related to the index
     * @param i the number of the intermediate index
     * @throws IOException exception related to the buffer handling and close
     */
    private static void readBufferLine(int i) throws IOException{
        String line = buffers.get(i).readLine();

        // If the buffer is empty, close it and set it to null for the pointers
        if(line == null){
            buffers.get(i).close();
            buffers.set(i, null);
            nextTerm.set(i, null);
            nextPostingList.set(i, null);

            // Decrease the number of open indexes
            openIndexes--;
            return;
        }

        // Create the new posting list
        PostingList list = new PostingList(line);

        // Update the correct entry of the lists
        nextTerm.set(i, list.getTerm());
        nextPostingList.set(i, list);
    }

    /**
     * Return the minimum term of the nextTerm list in lexicographical order
     * @return the next term to process
     */
    private static String getMinTerm(){
        String term = nextTerm.get(0);
        for(String elem: nextTerm){
            if(elem.compareTo(term) < 0){
                term = elem;
            }
        }
        return term;
    }

    /**
     * Find the min term between the indexes creates
     * the whole posting list and vocabulary entry for that term
     * stores them in memory. Update the pointers by scanning the
     * intermediate indexes.
     * @return true if the merging is complete, false otherwise
     */
    public static boolean mergeIndexes(){
        try {
            initialize();

            int memOffset = 0;

            while (openIndexes > 0){
                String termToProcess = getMinTerm();
                PostingList finalList = new PostingList();
                finalList.setTerm(termToProcess);

                
                VocabularyEntry vocabularyEntry = new VocabularyEntry(termToProcess);
                for(int i = 0; i < Utility.getNumIndexes(); i++){
                    // Found matching term
                    if(nextTerm.get(i) != null && nextTerm.get(i).equals(termToProcess)){
                        PostingList list = nextPostingList.get(i);

                        // Append the posting list to the final posting list of the term
                        finalList.appendPostings(list.getPostings());

                        //Update the lists for the scanned index
                        readBufferLine(i);

                        //update vocabulary statistics
                        vocabularyEntry.updateStatistics(list);
                    }
                }
                //the list for the term is computed, save it on disk and compute the information to store in vocabulary
                int memorySize = finalList.saveToDisk(memOffset);

                // Write to the vocabulary space occupancy and memory offset of the Posting List
                vocabularyEntry.setMemorySize(memorySize);
                vocabularyEntry.setMemoryOffset(memOffset);

                // Compute the final IDF
                vocabularyEntry.computeIDF();

                // Save vocabulary entry to file
                vocabularyEntry.saveToDisk();

//              vocabulary.add(vocabularyEntry);
                memOffset += memorySize;
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
