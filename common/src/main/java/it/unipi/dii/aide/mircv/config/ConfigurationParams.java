package it.unipi.dii.aide.mircv.config;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;


public class ConfigurationParams {
    private static String rawCollectionPath;
    private static String loadedCollectionPath;
    private static String stopwordsPath;
    private static String processedCollectionPath;
    private static String documentIndexPath;
    private static String partialIndexPath;
    private static String vocabularyPath;
    private static String invertedIndexPath;

    static {
        try {
            // Create the Document Builder and parse the config file
            File file = new File("config/config.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            // Call the normalize method on the Text nodes
            doc.getDocumentElement().normalize();

            // Retrieve the config params
            rawCollectionPath = doc.getElementsByTagName("rawCollectionPath").item(0).getTextContent();
            loadedCollectionPath = doc.getElementsByTagName("loadedCollectionPath").item(0).getTextContent();
            stopwordsPath = doc.getElementsByTagName("stopwordsPath").item(0).getTextContent();
            processedCollectionPath = doc.getElementsByTagName("processedCollectionPath").item(0).getTextContent();
            documentIndexPath = doc.getElementsByTagName("documentIndexPath").item(0).getTextContent();
            partialIndexPath = doc.getElementsByTagName("partialIndexPath").item(0).getTextContent();
            vocabularyPath = doc.getElementsByTagName("vocabularyPath").item(0).getTextContent();
            invertedIndexPath = doc.getElementsByTagName("invertedIndexPath").item(0).getTextContent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRawCollectionPath() {
        return rawCollectionPath;
    }

    public static String getLoadedCollectionPath() {
        return loadedCollectionPath;
    }

    public static String getStopwordsPath() {
        return stopwordsPath;
    }

    public static String getProcessedCollectionPath() {
        return processedCollectionPath;
    }

    public static String getVocabularyPath() {
        return vocabularyPath;
    }

    public static String getInvertedIndexPath() {
        return invertedIndexPath;
    }

    public static String getDocumentIndexPath() {
        return documentIndexPath;
    }

    public static String getPartialIndexPath() {
        return partialIndexPath;
    }
}
