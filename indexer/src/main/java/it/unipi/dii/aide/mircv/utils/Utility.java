package it.unipi.dii.aide.mircv.utils;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import static  it.unipi.dii.aide.mircv.utils.FileUtils.removeFile;

public class Utility {
    /**
     * Path of the document index
     */
    private static final String DOC_INDEX_PATH = ConfigurationParams.getDocumentIndexPath();
    private static final String VOCABULARY_PATH = ConfigurationParams.getVocabularyPath();
    private static final String INVERTED_INDEX_PATH = ConfigurationParams.getInvertedIndexPath();
    private static final String PARTIAL_INDEX_PATH = ConfigurationParams.getPartialIndexPath();


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

    /**
     * Function to initialize the files
     */
    public static void initializeFiles() {
        removeFile(DOC_INDEX_PATH);
        removeFile(VOCABULARY_PATH);
        removeFile(INVERTED_INDEX_PATH);
        removeFile(PARTIAL_INDEX_PATH);
    }
}
