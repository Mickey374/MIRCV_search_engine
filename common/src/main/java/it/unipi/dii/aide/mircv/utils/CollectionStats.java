package it.unipi.dii.aide.mircv.utils;


/**
* Class to maintain the collection Statistics during the application time
 */
public class CollectionStats {
    /**
     * Number of documents in the collection
     */
    private static int numDocuments = 0;

    public static int getNumDocuments() {
        return numDocuments;
    }

    public static void setNumDocuments(int numDocuments) {
        CollectionStats.numDocuments = numDocuments;
    }

    /**
     * Add a document to the number of documents
     */
    public static void addDocument(){
        numDocuments++;
    }
}
