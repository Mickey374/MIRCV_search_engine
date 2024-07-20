package it.unipi.dii.aide.mircv.config;

import java.io.*;

/**
 * The class that contains the collection size
 */
public class CollectionSize {
   /**
    * The size of the collection
    */
    private static long collectionSize;

    /**
     * The size of the vocabulary
     */
    private static long vocabularySize;

    /**
     * Sum of the length of all documents
     */
    private static long totalDocLen;

    /**
     * The path to the file storing the collection size
     */
    private static String COLLECTION_STATISTICS_PATH = ConfigurationParams.getCollectionStatisticsPath();

    static {
        if(!readFile()){
            collectionSize = 0;
            vocabularySize = 0;
            totalDocLen = 0;
        }
    }

    /**
     * Read the collectionStatistics file
     * @return true if the file is read correctly, false otherwise
     */
    private static boolean readFile() {
        if(COLLECTION_STATISTICS_PATH == null)
            return false;

        File file = new File(COLLECTION_STATISTICS_PATH);
        if(!file.exists())
            return false;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            collectionSize = ois.readLong();
            vocabularySize = ois.readLong();
            totalDocLen = ois.readLong();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return the size of the collection
     */
    public static long getCollectionSize() {
        return collectionSize;
    }

    /**
     * Return the size of the vocabulary
     */
    public static long getVocabularySize() {
        return vocabularySize;
    }

    /**
     * Return the total length of all documents
     */
    public static long getTotalDocLen() {
        return totalDocLen;
    }

    /**
     * Write the collectionStatistics file
     * @return true if the file is written correctly, false otherwise
     */
    public static boolean writeFile() {
        File file = new File(COLLECTION_STATISTICS_PATH);

        if (file.exists())
            if (!file.delete())
                return false;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeLong(collectionSize);
            oos.writeLong(vocabularySize);
            oos.writeLong(totalDocLen);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update the collection size and save the value on disk
     * @param size the new size
     * @return true if the file is written correctly, false otherwise
     */
    public static boolean updateCollectionSize(long size) {
        collectionSize = size;
        return writeFile();
    }

    /**
     * Update the vocabulary size and save the value on disk
     * @param size the new size
     * @return true if the file is written correctly, false otherwise
     */
    public static boolean updateVocabularySize(long size) {
        vocabularySize = size;
        return writeFile();
    }

    /**
     * Update the total length of all documents and save the value on disk
     * @param len the new size
     * @return true if the file is written correctly, false otherwise
     */
    public static boolean updateTotalDocLen(long len) {
        totalDocLen = len;
        return writeFile();
    }

    /**
     * Set the collection size
     * @param totalDocLen the new collection size
     */
    public static void setTotalDocLen(long totalDocLen) {
        CollectionSize.totalDocLen = totalDocLen;
    }

    /** needed for testing purposes
     * @param collectionStatisticsPath: path to be set
     */
    public static void setCollectionStatisticsPath(String collectionStatisticsPath) {
        COLLECTION_STATISTICS_PATH = collectionStatisticsPath;
    }

    /** needed for testing purposes
     * @param size: path to be set
     */
    public static void setCollectionSize(int size) {
        CollectionSize.collectionSize = size;
    }
}
