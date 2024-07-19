package it.unipi.dii.aide.mircv.config;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ConfigurationParams {
    private static String rawCollectionPath;
    private static String compressedCollectionPath;
    private static String stopwordsPath;
    private static String documentIndexPath;
    private static String vocabularyPath;
    private static String invertedIndexFreqs;
    private static String invertedIndexDocs;
    private static String partialVocabularyDir;
    private static String frequencyFileName;
    private static String docidsFileName;
    private static String vocabularyFilename;
    private static String frequencyDir;
    private static String docidsDir;
    private static String collectionStatisticsPath;
    private static String blockDescriptorsPath;
    private static String flagsFilePath;

    static {
        try {
            // Create the Document Builder and parse the config file
            File file = new File("config/config.xml");

            if (file.exists()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                // Call the normalize method on the Text nodes
                doc.getDocumentElement().normalize();

                // Retrieve the config params
                rawCollectionPath = doc.getElementsByTagName("rawCollectionPath").item(0).getTextContent();
                compressedCollectionPath = doc.getElementsByTagName("compressedCollectionPath").item(0).getTextContent();
                stopwordsPath = doc.getElementsByTagName("stopwordsPath").item(0).getTextContent();
                documentIndexPath = doc.getElementsByTagName("documentIndexPath").item(0).getTextContent();
                vocabularyPath = doc.getElementsByTagName("vocabularyPath").item(0).getTextContent();
                invertedIndexDocs = doc.getElementsByTagName("invertedIndexDocs").item(0).getTextContent();
                invertedIndexFreqs = doc.getElementsByTagName("invertedIndexFreqs").item(0).getTextContent();
                partialVocabularyDir = doc.getElementsByTagName("partialVocabularyDir").item(0).getTextContent();
                frequencyFileName = doc.getElementsByTagName("frequencyFileName").item(0).getTextContent();
                docidsFileName = doc.getElementsByTagName("docidsFileName").item(0).getTextContent();
                vocabularyFilename = doc.getElementsByTagName("vocabularyFilename").item(0).getTextContent();
                frequencyDir = doc.getElementsByTagName("frequencyDir").item(0).getTextContent();
                docidsDir = doc.getElementsByTagName("docidsDir").item(0).getTextContent();
                collectionStatisticsPath = doc.getElementsByTagName("collectionStatisticsPath").item(0).getTextContent();
                blockDescriptorsPath = doc.getElementsByTagName("blockDescriptorsPath").item(0).getTextContent();
                flagsFilePath = doc.getElementsByTagName("flagsFilePath").item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRawCollectionPath() {
        return rawCollectionPath;
    }

    public static String getCompressedCollectionPath() {
        return compressedCollectionPath;
    }

    public static String getStopwordsPath() {
        return stopwordsPath;
    }

    public static String getDocumentIndexPath() {
        return documentIndexPath;
    }

    public static String getVocabularyPath() {
        return vocabularyPath;
    }

    public static String getCollectionStatisticsPath() {
        return collectionStatisticsPath;
    }

    public static String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }

    public static String getFrequencyFileName() {
        return frequencyFileName;
    }

    public static String getDocidsFileName() {
        return docidsFileName;
    }

    public static String getFrequencyDir() {
        return frequencyDir;
    }

    public static String getDocidsDir() {
        return docidsDir;
    }

    public static String getVocabularyFilename() {
        return vocabularyFilename;
    }

    public static String getInvertedIndexFreqs() {
        return invertedIndexFreqs;
    }

    public static String getInvertedIndexDocs() {
        return invertedIndexDocs;
    }

    public static String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    public static String getFlagsFilePath() {
        return flagsFilePath;
    }
}