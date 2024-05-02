package it.unipi.dii.aide.mircv.utils;

public class CollectionStats {
    private static int numDocuments = 0;

    public static int getNumDocuments() {
        return numDocuments;
    }

    public static void setNumDocuments(int numDocuments) {
        CollectionStats.numDocuments = numDocuments;
    }

    public static void addDocument(){
        numDocuments++;
    }
}
