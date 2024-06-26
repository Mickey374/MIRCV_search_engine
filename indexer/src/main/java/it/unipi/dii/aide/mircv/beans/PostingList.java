package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import static it.unipi.dii.aide.mircv.utils.FileUtils.createIfNotExists;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class PostingList {

    private String term;
    private final ArrayList<Map.Entry<Integer, Integer>> postings = new ArrayList<>();
    private final static String PATH_TO_INVERTED_INDEX = ConfigurationParams.getInvertedIndexPath();

    public PostingList(String toParse) {
        String[] termRow = toParse.split("\t");
        this.term = termRow[0];
        parsePostings(termRow[1]);
    }

    public PostingList(){}

    private void parsePostings(String rawPostings){
        String[] documents = rawPostings.split(" ");
        for(String elem: documents){
            String[] posting = elem.split(":");
            postings.add(new AbstractMap.SimpleEntry<>(Integer.parseInt(posting[0]), Integer.parseInt(posting[1])));
        }
    }

    public String getTerm() {
        return term;
    }

    public ArrayList<Map.Entry<Integer, Integer>> getPostings(){
        return postings;
    }

    public String setTerm(String term){
        this.term = term;
        return term;
    }

    public void appendPostings(ArrayList<Map.Entry<Integer, Integer>> newPostings){
        postings.addAll(newPostings);
    }

    /**
     * Save the Posting lists as a 2 byte array: 1. DocIds 2. Frequencies
     * @param memoryOffset the memory offset in the inverted file at which posting
     * list will be stored.
     * @return
     */
    public int saveToDisk(long memoryOffset){
        // For each posting, we store the docId and the freq.
        // Each integer will occupy 4 bytes since integers are stored in byte arrays
        int numBytes = postings.size()*4*2;

        // Create the inverted index's file if not exists
        createIfNotExists(PATH_TO_INVERTED_INDEX);

        // Try to open the file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_INVERTED_INDEX), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)){
            // Instantiate the MappedByteBuffer for integer list of docIds
            MappedByteBuffer docsBuf = fChan.map(FileChannel.MapMode.READ_WRITE, memoryOffset, numBytes/2);

            // Instantiate the MappedByteBuffer for integer list of freqs
            MappedByteBuffer freqBuf = fChan.map(FileChannel.MapMode.READ_WRITE, memoryOffset, numBytes/2);

            // Check if the MappedByteBuffers are correctly instantiated
            if (docsBuf != null && freqBuf != null) {
                // Write postings to file
                for (Map.Entry<Integer, Integer> posting : postings) {
                    // Encode the docId
                    docsBuf.putInt(posting.getKey());
                    // Encode the freq
                    freqBuf.putInt(posting.getValue());
                }
                return numBytes;
            }
        } catch (InvalidPathException e){
            System.out.print("Path Error: " + e);
        } catch (IOException e) {
            System.out.println("I/O Error " + e);
        }
        return -1;
    }
}
