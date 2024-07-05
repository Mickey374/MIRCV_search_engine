package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.VocabularyEntry;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.Utility;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import java.util.*;

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
     * Number of intermediate indexes to process
     */
    private static final int NUM_INTERMEDIATE_INDEXES = Utility.getNumIndexes();

    /**
     * Path to the vocabulary file for Map DB
     */
    private static final String PATH_TO_VOCABULARY = ConfigurationParams.getVocabularyPath();

    /**
     * Memory mapped Intermediate indexes to be merged
     */
    private static final Map<Integer, Iterator<PostingList>> intermediateIndexes = new HashMap<>();

    /**
     * Vocabulary list to store the vocabulary entries
     */
    private static List<VocabularyEntry> vocabulary;


    // TODO: Convert the NUM_INVERTED_INDEXES mapped databases into a single collection of intermediate inverted indexes


    /**
     * Method that initializes the data structures
     * @param dbInd the inverted index database
     * @param dbVoc the vocabulary database
     */
    private static void initialize(DB dbInd, DB dbVoc) {
        // Initialize the vocabulary list
        vocabulary = (List<VocabularyEntry>) dbVoc.indexTreeList("vocabulary", Serializer.JAVA).createOrOpen();

        for (int i = 0; i < NUM_INTERMEDIATE_INDEXES; i++) {
            List<PostingList> list = (List<PostingList>) dbInd.indexTreeList("index_" + i, Serializer.JAVA).createOrOpen();
            intermediateIndexes.put(i, list.iterator());
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
            if (intermediateIndexes.get(i) == null || intermediateIndexes.get(i).hasNext())
                continue;

            // Next term to be processed at the intermediate index 'i'
            String nextTerm = intermediateIndexes.get(i).next().getTerm();

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
            if (intermediateIndexes.get(i) != null && intermediateIndexes.get(i).hasNext()) {
                // Get the posting list from the intermediate index
                PostingList intermediatePostingList = intermediateIndexes.get(i).next();

                if(intermediatePostingList.getTerm().equals(term)) {
                    // Compute the memory occupancy of the posting list
                    numBytes += intermediatePostingList.getNumBytes();

                    // Append the posting list to the final posting list of the term
                    finalList.appendPostings(intermediatePostingList.getPostings());

                    // Update vocabulary statistics
                    vocabularyEntry.updateStatistics(intermediatePostingList);


                    // Check if the intermediate index is empty
                    if (intermediateIndexes.get(i).hasNext()) {
                        intermediateIndexes.replace(i, null);

                    }
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
        try (DB dbInd = DBMaker.fileDB(INTERMEDIATE_INDEX_PATH).fileChannelEnable().fileMmapEnable().make();
             DB dbVoc = DBMaker.fileDB(PATH_TO_VOCABULARY).fileChannelEnable().fileMmapEnable().make()){

            // Initialize the data structures
            initialize(dbInd, dbVoc);

            // Process the intermediate indexes
            while (true) {
                // Get the minimum term in lexicographical order
                String termToProcess = getMinTerm();

                if(termToProcess == null)
                    break;

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