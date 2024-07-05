package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.VocabularyEntry;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.Utility;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Class implementing the merge of the Single Pass In Memory Indexing algorithm for Intermediate Posting Lists
 */
public class Merger {
    /**
     * Memory offset for the inverted index
     */
    private static long memOffset = 0;

    /**
     * Standard pathname for intermediate index files.
     */
    private static final String INTERMEDIATE_INDEX_PATH = ConfigurationParams.getPartialIndexPath();

    /**
     * Num of Open Indexes to process: When 0, means all indexes are processed
     */
    private static int openIndexes;

    /**
     * Number of intermediate indexes to process
     */
    private static final int NUM_INTERMEDIATE_INDEXES = Utility.getNumIndexes();

    /**
     * Path to the vocabulary file for Map DB
     */
    private static final String PATH_TO_VOCABULARY = ConfigurationParams.getVocabularyPath();

    /**
     * Path to the inverted index file for Map DB
     */
    private static final ArrayList<DB> mapped_dbs = new ArrayList<>();

    /**
     * Memory mapped Intermediate indexes to be merged
     */
    private static final Map<Integer, ArrayList<PostingList>> intermediateIndexes = new HashMap<>();

    /**
     * Vocabulary list to store the vocabulary entries
     */
    private static ArrayList<VocabularyEntry> vocabulary;


    // TODO: Convert the NUM_INVERTED_INDEXES mapped databases into a single collection of intermediate inverted indexes


    /**
     * Buffers for the intermediate indexes
     */
    private static void initialize() {
        openIndexes = NUM_INTERMEDIATE_INDEXES;

        for (int i = 0; i < openIndexes; i++) {
            // Try to open intermediate index 'i' file
            try {
                DB db = DBMaker.fileDB(INTERMEDIATE_INDEX_PATH + i + ".db").fileChannelEnable().fileMmapEnable().make();
                mapped_dbs.add(db);
                intermediateIndexes.put(i, (ArrayList<PostingList>) db.indexTreeList("index_" + i, Serializer.JAVA).createOrOpen());
            } catch (Exception e) {
                e.printStackTrace();
                mapped_dbs.add(i, null);
                openIndexes--;
            }
        }
    }

    /**
     * Method that cleans the data structures and closes the buffers
     */
    private static void clean() {
        // TODO: Change this to a single mapped db for multiple indexes
        // Close the map databases for each mapped db
        for (int i = 0; i < NUM_INTERMEDIATE_INDEXES; i++) {
            mapped_dbs.get(i).close();
        }
    }


    /**
     * Return the minimum term of the nextTerm list in lexicographical order
     *
     * @return the next term to process
     */
    private static String getMinTerm() {
        String term = null;

        for (int i = 0; i < NUM_INTERMEDIATE_INDEXES; i++) {
            // If the term is not null and the term is less than the current term
            if (intermediateIndexes.get(i) == null || intermediateIndexes.get(i).get(0) == null)
                continue;

            // Next term to be processed at the intermediate index 'i'
            String nextTerm = intermediateIndexes.get(i).get(0).getTerm();

            // If the term is null, skip to the next term
            if (term == null) {
                term = nextTerm;
                continue;
            }

            // If the next term is less than the current term, update the term
            if (nextTerm.compareTo(term) < 0)
                term = nextTerm;
        }
        return term;
    }

    /**
     * Process the term and merge the posting lists
     *
     * @param term the term to process
     * @return the final posting list for the term
     */
    public static PostingList processTerm(String term) {
        // New posting list for the term
        PostingList finalList = new PostingList();
        finalList.setTerm(term);

        // new Vocabulary entry for the term
        VocabularyEntry vocabularyEntry = new VocabularyEntry(term);

        // Total space occupied in bytes by the posting list
        long numBytes = 0;

        // Iterate over the intermediate indexes
        for (int i = 0; i < NUM_INTERMEDIATE_INDEXES; i++) {
            // If a matching term is found
            if (intermediateIndexes.get(i) != null && intermediateIndexes.get(i).get(0) != null && intermediateIndexes.get(i).get(0).getTerm().equals(term)) {
                // Get the posting list from the intermediate index
                PostingList intermediatePostingList = intermediateIndexes.get(i).get(0);

                // Compute the memory occupancy of the posting list
                numBytes += intermediatePostingList.getNumBytes();

                // Append the posting list to the final posting list of the term
                finalList.appendPostings(intermediatePostingList.getPostings());

                // Update vocabulary statistics
                vocabularyEntry.updateStatistics(intermediatePostingList);

                // Update the lists for the scanned index
                intermediateIndexes.get(i).remove(0);

                // Check if the intermediate index is empty
                if (intermediateIndexes.get(i).get(0) == null) {
                    intermediateIndexes.replace(i, null);

                    // Decrease the number of open indexes
                    openIndexes--;
                }
            }
        }
        // Writing to vocabulary the space occupancy and the memory offset for the posting list
        vocabularyEntry.setMemorySize((int) numBytes);
        vocabularyEntry.setMemoryOffset((int) memOffset);

        // Compute final IDF
        vocabularyEntry.computeIDF();

        // Add Vocabulary entry to the vocabulary list
        vocabulary.add(vocabularyEntry);

        // Return the final posting list
        return finalList;
    }

    public static boolean mergeIndexes() {
        try (DB db = DBMaker.fileDB(PATH_TO_VOCABULARY).fileChannelEnable().fileMmapEnable().make()) {
            // Create the vocabulary list
            vocabulary = (ArrayList<VocabularyEntry>) db.indexTreeList("vocabulary", Serializer.JAVA).createOrOpen();

            // Initialize the data structures
            initialize();


            // Process the intermediate indexes
            while (openIndexes > 0) {
                // Get the minimum term in lexicographical order
                String termToProcess = getMinTerm();

                // Process the termToProcess
                PostingList mergedPostingList = processTerm(termToProcess);

                // Save the posting list to disk
                int memorySize = mergedPostingList.saveToDisk(memOffset);

                // Update the memory offset
                memOffset += memorySize;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
