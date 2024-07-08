package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import static it.unipi.dii.aide.mircv.utils.FileUtils.createIfNotExists;

import java.io.IOException;
import java.io.Serializable;
import java.io.Serial;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class PostingList implements Serializable{

    private String term;
    private ArrayList<Map.Entry<Integer, Integer>> postings = new ArrayList<>();
    private final static String PATH_TO_INVERTED_INDEX = ConfigurationParams.getInvertedIndexPath();

    public PostingList(String toParse) {
        String[] termRow = toParse.split("\t");
        this.term = termRow[0];
        if (termRow.length > 1)
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

    public void setTerm(String term){
        this.term = term;
    }

    public void appendPostings(ArrayList<Map.Entry<Integer, Integer>> newPostings){
        postings.addAll(newPostings);
    }

    /**
     * Function to save the posting list to disk
     * @param memoryOffset: the memory offset where the posting list will be saved
     * @return the number of bytes written to disk
     */
    public int saveToDisk(long memoryOffset, VocabularyEntry ve){
        // For each posting, we store the docId and the freq.
        // Each integer will occupy 4 bytes since integers are stored in byte arrays
        int numBytes = getNumBytes();

        // Try to open the file channel to the file of the inverted index
        try (FileChannel fChan = (FileChannel) Files.newByteChannel(Paths.get(PATH_TO_INVERTED_INDEX), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)){
            // Instantiate the MappedByteBuffer for integer list of docIds
           MappedByteBuffer buffer = fChan.map(FileChannel.MapMode.READ_WRITE, memoryOffset, numBytes);

            // Check if the MappedByteBuffers are correctly instantiated
            if (buffer != null) {
                // Write postings to file
                for (Map.Entry<Integer, Integer> posting : postings) {
                    // Encode the docId
                    buffer.putInt(posting.getKey());
                }
                // Update the memory offset
                long freqOffset = buffer.position();

                ve.setFrequencyOffset(freqOffset);

                // Write the frequency to the file
                for (Map.Entry<Integer, Integer> posting : postings) {
                    // Encode the frequency
                    buffer.putInt(posting.getValue());
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

    /**
     * method to get the number of bytes occupied by the posting list when stored in memory
     * @return the number of bytes occupied by the posting list in bytes
     */
    public int getNumBytes(){
        return postings.size()*4*2;
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "term='" + term + '\'' +
                ", postings=" + postings +
                '}';
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeUTF(term);
        out.writeObject(postings);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        term = in.readUTF();
        postings = (ArrayList<Map.Entry<Integer, Integer>>) in.readObject();
    }
}
