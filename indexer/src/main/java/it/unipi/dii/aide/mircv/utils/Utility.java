package it.unipi.dii.aide.mircv.utils;

public class Utility {
    /**
     * Number of different intermediate indexes
     */
    private static int numIndexes = 0;

    public static int getNumIndexes() {
        return numIndexes;
    }

    public static void setNumIndexes(int numIndexes) {
        Utility.numIndexes = numIndexes;
    }
}
