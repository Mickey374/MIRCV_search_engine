package it.unipi.dii.aide.mircv.config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * The class that contains the flags
 */
public class Flags {
    /**
     * The flag for the collection size
     */
    private static final String FLAGS_FILE_PATH = ConfigurationParams.getFlagsFilePath();

    /**
     * Flag for enabling the compression
     */
    private static boolean compression;

    /**
     * Flag for enabling the stemming
     */
    private static boolean stemStopRemoval;

    /**
     * Flag for enabling the max score algorithm to score queries
     */
    private static boolean maxScore;

    /**
     * Read flags from file and initialize the relative booleans
     * @return true if the file is read correctly, false otherwise
     */
    public static boolean initializeFlags() {
        if(FLAGS_FILE_PATH == null)
            return false;

        try(    FileInputStream fis = new FileInputStream(FLAGS_FILE_PATH);
                DataInputStream dis = new DataInputStream(fis)) {
            // read the flags from file
            compression = dis.readBoolean();
            stemStopRemoval = dis.readBoolean();
            maxScore = dis.readBoolean();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * updates the flag status and save them to file
     *
     * @param compressionFlag     to set compression
     * @param stemStopRemovalFlag to set setemming and stopword removal
     * @param maxScoreFlag        to set max score algorithm
     * @return true if successful
     */
    public static boolean saveFlags(boolean compressionFlag, boolean stemStopRemovalFlag, boolean maxScoreFlag) {
        if (FLAGS_FILE_PATH == null)
            return false;

        try (FileOutputStream fos = new FileOutputStream(FLAGS_FILE_PATH);
             DataOutputStream dos = new DataOutputStream(fos)) {

            // update the flags
            compression = compressionFlag;
            stemStopRemoval = stemStopRemovalFlag;
            maxScore = maxScoreFlag;

            // write the flags to file
            dos.writeBoolean(compressionFlag);
            dos.writeBoolean(stemStopRemovalFlag);
            dos.writeBoolean(maxScoreFlag);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return the status of the compression flag
     * @return true if compression is enabled, false otherwise
     */
    public static boolean isCompressionEnabled() {
        return compression;
    }

    /**
     * Return the status of the stemming and stopword removal flag
     * @return true if stemming and stopword removal are enabled, false otherwise
     */
    public static boolean isStemStopRemovalEnabled() {
        return stemStopRemoval;
    }

    /**
     * Return the status of the max score algorithm flag
     * @return true if the max score algorithm is enabled, false otherwise
     */
    public static boolean isMaxScoreEnabled() {
        return maxScore;
    }

    /**
     * Set the status of the compression flag
     * @param compressionFlag the new status of the compression flag
     */
    public static void setCompression(boolean compressionFlag) {
        Flags.compression = compressionFlag;
    }

    /**
     * Set the status of the stemming and stopword removal flag
     * @param stemStopRemovalFlag the new status of the stemming and stopword removal flag
     */
    public static void setStemStopRemoval(boolean stemStopRemovalFlag) {
        Flags.stemStopRemoval = stemStopRemovalFlag;
    }

    /**
     * Set the status of the max score algorithm flag
     * @param maxScoreFlag the new status of the max score algorithm flag
     */
    public static void setMaxScore(boolean maxScoreFlag) {
        Flags.maxScore = maxScoreFlag;
    }
}