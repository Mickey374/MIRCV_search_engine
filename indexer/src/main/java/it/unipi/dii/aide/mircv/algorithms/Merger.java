package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.VocabularyEntry;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.Utility;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import java.util.*;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Class implementing the merge of the Single Pass In Memory Indexing algorithm for Intermediate Posting Lists
 */
public class Merger {
    /**
     * Memory offset for the inverted index
     */
    private static long memOffset = 0;

    /**
     * Standard pathname for intermediate index files.
     */
    private static final String PARTIAL_INDEX_PATH = ConfigurationParams.getPartialIndexPath();

    /**
     * Number of intermediate indexes to process
     */
//    private static final int NUM_INTERMEDIATE_INDEXES = Utility.getNumIndexes();
    private static int numIndexes;

    /**
     * Path to the vocabulary file for Map DB
     */
    private static final String PATH_TO_VOCABULARY = ConfigurationParams.getVocabularyPath();

    /**
     * Memory mapped Intermediate indexes to be merged
     */
    private static final Map<Integer, Iterator<PostingList>> intermediateIndexes = new HashMap<>();

    /**
     * Vocabulary list to store the vocabulary entries
     */
    private static Map<String, VocabularyEntry> vocabulary;

    private final static String PATH_TO_INVERTED_INDEX = ConfigurationParams.getInvertedIndexPath();

    private static PostingList[] nextLists = null;


    /**
     * Method that initializes the data structures
     * @param dbInd the inverted index database
     * @param dbVoc the vocabulary database
     */
    private static void initialize(DB dbInd, DB dbVoc) {
        // Initialize the vocabulary list
        vocabulary = (Map<String, VocabularyEntry>) dbVoc.hashMap("vocabulary")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        nextLists = new PostingList[numIndexes];

        for (int i = 0; i < numIndexes; i++) {
            List<PostingList> list = (List<PostingList>) dbInd.indexTreeList("index_" + i, Serializer.JAVA).createOrOpen();
            intermediateIndexes.put(i, list.iterator());
            nextLists[i] = intermediateIndexes.get(i).next();
        }
    }


    /**
     * Return the minimum term of the nextTerm list in lexicographical order
     *
     * @return the next term to process
     */
    private static String getMinTerm() {
        String term = null;

        for (int i = 0; i < numIndexes; i++) {
            // If the term is not null and the term is less than the current term
            if (nextLists[i] == null)
                continue;

            // Next term to be processed at the intermediate index 'i'
            String nextTerm = nextLists[i].getTerm();

            // If the term is null, skip to the next term
            if (term == null) {
                term = nextTerm;
                continue;
            }

            // If the next term is less than the current term, update the term
            if (nextTerm.compareTo(term) < 0)
                term = nextTerm;
        }
        return term;
    }

    /**
     * Process the term and merge the posting lists
     *
     * @param term the term to process
     * @return the final posting list for the term
     */
    public static PostingList processTerm(String term) {
        // New posting list for the term
        PostingList finalList = new PostingList();
        finalList.setTerm(term);

        // new Vocabulary entry for the term
        VocabularyEntry vocabularyEntry = new VocabularyEntry(term);

        // Total space occupied in bytes by the posting list
        long numBytes = 0;

        // Iterate over the intermediate indexes
        for (int i = 0; i < numIndexes; i++) {
            // If a matching term is found
            if (nextLists[i] != null) {
                // Get the posting list from the intermediate index
                PostingList intermediatePostingList = nextLists[i];

                if(intermediatePostingList.getTerm().equals(term)) {
                    // Compute the memory occupancy of the posting list
                    numBytes += intermediatePostingList.getNumBytes();

                    // Append the posting list to the final posting list of the term
                    finalList.appendPostings(intermediatePostingList.getPostings());

                    // Update vocabulary statistics
                    vocabularyEntry.updateStatistics(intermediatePostingList);


                    // Check if the intermediate index is empty
                    if (intermediateIndexes.get(i).hasNext()) {
                        nextLists[i] = intermediateIndexes.get(i).next();
                    } else {
                        nextLists[i]  = null;
                    }
                }
            }
        }
        // Writing to vocabulary the space occupancy and the memory offset for the posting list
        vocabularyEntry.setMemorySize((int) numBytes);
        vocabularyEntry.setMemoryOffset((int) memOffset);

        // Compute final IDF
        vocabularyEntry.computeIDF();

        // Add Vocabulary entry to the vocabulary list
        vocabulary.put(term, vocabularyEntry);

        // Return the final posting list
        return finalList;
    }

    /**
     * Save the posting list to disk
     * @param list the posting list to save
     * @return the number of bytes written to disk
     */
    private static long saveToDisk(PostingList list) {
        // Memory occupancy for the posting list:
        // 2 integers for each posting (docId and freq)
        // 4 bytes for each integer (32 bits)
        int numBytes = list.getNumBytes();

        // Try to open the file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_INVERTED_INDEX), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)){
            // Instantiate the MappedByteBuffer for integer list of docIds
            MappedByteBuffer buffer = fChan.map(FileChannel.MapMode.READ_WRITE, memOffset, numBytes);

            // Check if the MappedByteBuffers are correctly instantiated
            if (buffer != null) {
                // Write postings to file
                for (Map.Entry<Integer, Integer> posting : list.getPostings()) {
                    buffer.putInt(posting.getKey());
                }
                long freqOffset = memOffset + buffer.position();

                // Set the frequency offset in the vocabulary
                vocabulary.get(list.getTerm()).setFrequencyOffset(freqOffset);

                // Write postings to file
                for (Map.Entry<Integer, Integer> posting : list.getPostings()) {
                    buffer.putInt(posting.getValue());
                }
                long memorySize = buffer.position();
                memOffset += memorySize;
                vocabulary.get(list.getTerm()).setMemorySize((int) memorySize);
                return memorySize;
            }
        } catch (InvalidPathException e) {
            System.out.print("Path Error: " + e);
        }
        catch (IOException e) {
            System.out.println("I/O Error " + e);
        }
        return -1;
    }

    public static boolean mergeIndexes(int numIndexes) {
        Merger.numIndexes = numIndexes;

        try (DB dbInd = DBMaker.fileDB(PARTIAL_INDEX_PATH).fileChannelEnable().fileMmapEnable().make();
             DB dbVoc = DBMaker.fileDB(PATH_TO_VOCABULARY).fileChannelEnable().fileMmapEnable().make()){

            // Initialize the data structures
            initialize(dbInd, dbVoc);

            // Process the intermediate indexes
            while (true) {
                // Get the minimum term in lexicographical order
                String termToProcess = getMinTerm();

                if(termToProcess == null)
                    break;

                System.out.println("Processing term: " + termToProcess);
                // Process the termToProcess
                PostingList mergedPostingList = processTerm(termToProcess);

                // Save the posting list to disk
                long memorySize = saveToDisk(mergedPostingList);
                System.out.println("Memory Offset: " + memOffset + " Memory Size: " + memorySize);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}