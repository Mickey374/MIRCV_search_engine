package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.algorithms.Merger;
import it.unipi.dii.aide.mircv.algorithms.Spimi;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.config.Flags;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static it.unipi.dii.aide.mircv.utils.Utility.initializeFiles;
import static it.unipi.dii.aide.mircv.utils.Utility.cleanUpFiles;

/**
 * main class for the indexing module
 */
public class Main {
    /**
     * starts the indexing, reading flags from the args
     * @param args args[0] -> compression flag
     */
    public static void main(String[] args) {
        // If set to true, read from compressed file as enabled
        boolean compressedReadingEnable = false;

        // If set to true, write to compressed file as enabled
        boolean compressedWritingEnable = false;

        // If set to true, stem and stop removal are enabled
        boolean stemStopRemovalEnable = false;

        // If set to true, debug mode is enabled
        boolean debugModeEnable = false;

        // If set to true, maxScore is used else DAAT is used
        boolean maxScoreEnabled = false;

        // Check input and initialize flags
        for (String flag : args) {
            switch (flag) {
                case "-c":
                    compressedReadingEnable = true;
                    break;
                case "-w":
                    compressedWritingEnable = true;
                    break;
                case "-s":
                    stemStopRemovalEnable = true;
                    break;
                case "-d":
                    debugModeEnable = true;
                    break;
                case "-m":
                    maxScoreEnabled = true;
                    break;
                default:
                    System.out.println("Invalid flag: " + flag);
                    System.exit(1);
            }
        }

        // Save to file flags that will be useful for query handling
        if(!Flags.saveFlags(compressedWritingEnable, stemStopRemovalEnable, maxScoreEnabled)){
            System.out.println("Error while saving flags");
            System.exit(1);
        }

        // Initialize the files and directories for Spimi execution
        initializeFiles();

        // Execute the Spimi algorithm
        System.out.println("Indexing started with params: " + Arrays.toString(args));
        long start = System.currentTimeMillis();
        int numIndexes = Spimi.executeSpimi(compressedReadingEnable, debugModeEnable);

        // Check for errors
        if(numIndexes <= 0) {
            System.out.println("Error while executing Spimi: no partial indexes created");
            System.exit(1);
        }
        long spimiTime = System.currentTimeMillis();
        formatTime(start, spimiTime, "Spimi execution time");

        if(Merger.mergeIndexes(numIndexes, compressedWritingEnable, debugModeEnable)) {
            cleanUpFiles();

            // Print the time taken for the execution
            long end = System.currentTimeMillis();
            formatTime(spimiTime, end, "Merging execution time");
            formatTime(start, end, "Total execution time for Inverted index");

            FileUtils.createIfNotExists("data/indexerStatistics.tsv");
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("data/indexerStatistics.tsv", true));) {
                long docidSize = Files.size(Paths.get(ConfigurationParams.getInvertedIndexDocs()));
                long freqSize = Files.size(Paths.get(ConfigurationParams.getInvertedIndexFreqs()));
                long vocabularySize = Files.size(Paths.get(ConfigurationParams.getVocabularyPath()));
                long docIndexSize = Files.size(Paths.get(ConfigurationParams.getDocumentIndexPath()));
                long fullTime = end - start;
                String stats = Arrays.toString(args) + '\t' + fullTime + '\t' + docidSize + '\t' + freqSize + '\t' + vocabularySize + '\t' + docIndexSize + '\n';
                writer.write(stats);
            }catch(Exception e){
                e.printStackTrace();
            }

            Merger.printPerformanceStatistics();

            return;
        }
        System.out.println("Error while merging indexes");
        cleanUpFiles();
    }

    /**
     * formats the prints used when an indexing operation is completed
     * @param start     the start time
     * @param end       the stop time
     * @param operation the operation done
     */
    private static void formatTime(long start, long end, String operation) {
        int minutes = (int) ((end - start) / (1000 * 60));
        int seconds = (int) ((end - start) / 1000) % 60;

        if(seconds < 10)
            System.out.println(operation + "done in: " + minutes + ":0" + seconds);
        else
            System.out.println(operation + "done in: " + minutes + ":" + seconds);
    }
}