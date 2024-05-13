package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.utils.CollectionStats;
import java.util.Map;

/**
 * Entry of the vocabulary for a term
 */
public class VocabularyEntry {
    /**
     * Counter for the terms used in termId
     */
    private static int termCount = 0;

    /**
     * Term Id of the specific term
     */
    private int termid;

    /**
     * Term for the vocabulary entry
     */
    private String term;

    /**
     * Document frequency for the term
     */
    private int df = 0;

    /**
     * Term frequency of the term in the collection
     */
    private int tf = 0;

    /**
     * Inverse of the doc frequency of the term
     */
    private double idf = 0;

    /**
     * Starting point of the terms posting list in
     * the inverted index in bytes
     */
    private long memoryOffset = 0;

    /**
     * Size of the terms posting list in the
     * inverted index in bytes
     */
    private long memorySize = 0;

    /**
     * Constructor for the vocabulary entry for the
     * term passed as parameter.
     * Assign the termid to the term and initializes
     * all statistics and memory information
     * @param term the token of the entry
     */
    public VocabularyEntry(String term) {
        this.term = term;

        // Assign the termid and increase the counter
        this.termid = termCount;
        termCount++;
    }

    /**
     * Updates the statistics of the vocabulary
     * Updates tf and df with data of partial posting list
     * @param list the posting list from which we compute the stats
     */
    public void updateStatistics(PostingList list) {
        // for each element of the Intermediate posting list
        for(Map.Entry<Integer, Integer> posting: list.getPostings()){
            // Update the term frequency
            this.tf += posting.getValue();

            // Update the raw document frequency
            this.df++;
        }
    }

    /**
     * Compute the idf using the values computed during the merging of the index
     */
    private void computeIDF(){
        this.idf = Math.log10(CollectionStats.getNumDocuments()/(double)this.df);
    }

    public void computeMemoryOffsets() {
        // Implementation goes here
    }

    public void saveToDisk() {
        // Implementation goes here
    }

}
