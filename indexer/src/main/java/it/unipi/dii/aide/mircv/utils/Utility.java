package it.unipi.dii.aide.mircv.utils;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.config.Flags;
import it.unipi.dii.aide.mircv.preprocess.Preprocesser;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import static  it.unipi.dii.aide.mircv.utils.FileUtils.*;

public class Utility {
    /**
     * Path of the document index
     */
    private static final String DOC_INDEX_PATH = ConfigurationParams.getDocumentIndexPath();
    private static final String VOCABULARY_PATH = ConfigurationParams.getVocabularyPath();
    private static final String PARTIAL_INDEX_DOCIDS = ConfigurationParams.getDocidsDir();
    private static final String PARTIAL_INDEX_FREQS = ConfigurationParams.getFrequencyDir();
    private static final String INVERTED_INDEX_DOCIDS = ConfigurationParams.getInvertedIndexDocs();
    private static final String INVERTED_INDEX_FREQS = ConfigurationParams.getInvertedIndexFreqs();
    private static final String PARTIAL_VOCABULARY_PATH = ConfigurationParams.getPartialVocabularyDir();
    private static final String BLOCK_DESCRIPTORS_PATH = ConfigurationParams.getBlockDescriptorsPath();


    /**
     * deletes the older version of the inverted index and possible intermediate indexes,
     * and creates new folders for the partial indexes
     */
    public static void initializeFiles() {
        removeFile(DOC_INDEX_PATH);
        removeFile(VOCABULARY_PATH);
        removeFile(INVERTED_INDEX_DOCIDS);
        removeFile(INVERTED_INDEX_FREQS);
        removeFile(BLOCK_DESCRIPTORS_PATH);

        deleteDirectory(PARTIAL_INDEX_DOCIDS);
        deleteDirectory(PARTIAL_INDEX_FREQS);
        deleteDirectory(PARTIAL_VOCABULARY_PATH);
        deleteDirectory("data/debug");

        // create the directories for the partial indexes
        createDirectory(ConfigurationParams.getDocidsDir());
        createDirectory(ConfigurationParams.getFrequencyDir());
        createDirectory(ConfigurationParams.getPartialVocabularyDir());

        if(Flags.isStemStopRemovalEnabled())
            Preprocesser.readStopwords();
    }

    /**
     * Deletes the folders of the partial indexes when the indexing is done
     */
    public static void cleanUpFiles() {
        // Remove the partial index docids directory
        FileUtils.deleteDirectory(ConfigurationParams.getDocidsDir());

        // Remove the partial index frequency directory
        FileUtils.deleteDirectory(ConfigurationParams.getFrequencyDir());

        // Remove the partial vocabulary directory
        FileUtils.deleteDirectory(ConfigurationParams.getPartialVocabularyDir());
    }
}
