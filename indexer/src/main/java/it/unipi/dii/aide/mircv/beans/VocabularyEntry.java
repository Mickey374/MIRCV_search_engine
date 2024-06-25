package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.CollectionStats;
import java.util.Map;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static it.unipi.dii.aide.mircv.utils.FileUtils.createIfNotExists;

/**
 * Entry of the vocabulary for a term
 */
public class VocabularyEntry {
    /**
     * Counter for the terms used in termId
     */
    private static int termCount = 0;

    /**
     * termid of the specific term
     */
    private final int termid;

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

    private static final String PATH_TO_VOCABULARY = ConfigurationParams.getVocabularyPath();

    /**
     * Constructor for the vocabulary entry for the
     * term passed as parameter.
     * Assign the termid to the term and initializes
     * all statistics and memory information
     * @param term the token of the entry
     */
    public VocabularyEntry(String term) {
        // Assign the term
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
    public void computeIDF(){
        this.idf = Math.log10(CollectionStats.getNumDocuments()/(double)this.df);
    }


    /**
     * Appends the vocabulary entry in the vocabs file
     */
    public void saveToDisk() {
        // Create the file if not exists
        createIfNotExists(PATH_TO_VOCABULARY);

        // Save vocabulary entry to file using Append
        try {
            Files.writeString(Paths.get(PATH_TO_VOCABULARY), this.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the vocabulary entry as a String formatted in the ff format:
     * [termid]-[term]-[idf] [tf] [memoryOffset] [memorySize]
     * @return the formatted String
     */
    public String toString(){
        return termid + "-" + term + "-" +
                idf + " " +
                tf + " " +
                memoryOffset + " " +
                memorySize +
                '\n';
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    public void setMemoryOffset(int memoryOffset) {
        this.memoryOffset = memoryOffset;
    }
}
