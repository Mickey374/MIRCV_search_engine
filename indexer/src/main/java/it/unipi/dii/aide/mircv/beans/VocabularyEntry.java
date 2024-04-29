package it.unipi.dii.aide.mircv.beans;

public class VocabularyEntry {
    private static int termCount = 0;

    private int termid;

    private String term;

    private double idf;

    private int tf;

    private long memoryOffset;

    private long memorySize;

    public void updateStatistics(PostingList list) {
        // Implementation goes here
    }

    public void computeMemoryOffsets() {
        // Implementation goes here
    }

    public void saveToDisk() {
        // Implementation goes here
    }

}
