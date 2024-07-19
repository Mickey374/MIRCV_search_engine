package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DocumentIndexEntry {
    /**
     * Pid of the document
     */
    private String pid;

    /**
     * docid of a document
     */
    private int docid;

    /**
     * Length of the document
     */
    private int docLen;

    /**
     * Memory offset of the documentIndex file
     */
    private static long memOffset = 0;

    /**
     * Size of pid on disk
     */
    private static final int PID_SIZE = 64;

    /**
     * Number of bytes used to store the document index entry: 4 integers (16 bytes) + 1 string (variable length)
     */
    public static final int ENTRY_SIZE = PID_SIZE + 4 + 4;

    /**
     * Path to the documentIndex file
     */
    private static String PATH_TO_DOCUMENT_INDEX = ConfigurationParams.getDocumentIndexPath();

    /**
     * Default constructor with 0 params
     */
    public DocumentIndexEntry() {}

    /**
     * Constructor for the document index entry of a specific document
     * @param pid the pid of such document
     * @param docid the docid of such documents
     * @param docLen the length of such documents in terms of number of terms
     */
    public DocumentIndexEntry(String pid, int docid, int docLen) {
        this.pid = pid;
        this.docid = docid;
        this.docLen = docLen;
    }

    public static void setDocindexPath(String path) {
        PATH_TO_DOCUMENT_INDEX = path;
    }

    public String getPid() {return pid;}

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public int getDocLen() {
        return docLen;
    }

    public void setDocLen(int docLen) {
        this.docLen = docLen;
    }

    @Override
    public String toString() {
        return "DocumentIndexEntry: {" +
                "pid='" + pid + '\'' +
                ", docid=" + docid +
                ", document Length=" + docLen +
                '}';
    }

    /**
     * Write the document index entry to disk
     * @return the offset for the entry
     */
    public long writeToDisk() {
        // Write the entry to disk using a FileChannel
        try (FileChannel fc = (FileChannel) Files.newByteChannel(
                Paths.get(PATH_TO_DOCUMENT_INDEX),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            //Instantiate the MappedByte buffer for the entry
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, memOffset, ENTRY_SIZE);

            // If buffer is not created
            if (buffer == null)
                return -1;

            // Write the pid with size PID_SIZE
            CharBuffer cb = CharBuffer.allocate(PID_SIZE);
            for(int i = 0; i < this.pid.length(); i++)
                cb.put(i, this.pid.charAt(i));

            // Write the pid to the buffer
            buffer.put(StandardCharsets.UTF_8.encode(cb));

            // Write the docid
            buffer.putInt(this.docid);

            // Write the docLen
            buffer.putInt(this.docLen);

            // save the start offset of the structure
            long startOffset = memOffset;

            // update memory offset
            memOffset = memOffset + ENTRY_SIZE;

            return startOffset;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Read the document index entry from disk
     * @param memoryOffset the offset of the entry
     * @return true if the entry is read correctly, false otherwise
     */
    public boolean readFromDisk(long memoryOffset) {
        // Read the entry from disk using a FileChannel
        try (FileChannel fc = (FileChannel) Files.newByteChannel(
                Paths.get(PATH_TO_DOCUMENT_INDEX),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE)) {
            // Instantiate the MappedByte buffer for the entry
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset, PID_SIZE);

            // If buffer is not created
            if (buffer == null)
                return false;

            // Read from file into the charBuffer, then pass to the string
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            if (charBuffer.toString().split("\0").length == 0)
                return true;
            this.pid = charBuffer.toString().split("\0")[0];

            // Instantiate the buffer for reading other info
            buffer = fc.map(FileChannel.MapMode.READ_WRITE, memoryOffset + PID_SIZE, ENTRY_SIZE - PID_SIZE);

            // If buffer is not created
            if (buffer == null)
                return false;

            // Read the docid
            this.docid = buffer.getInt();

            // Read the docLen
            this.docLen = buffer.getInt();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the size of the entry
     * @return the size of the entry
     */
    public static int getEntrySize() {
        return ENTRY_SIZE;
    }

    /**
     * Update the document index path for testing
     */
    protected static void setTestPath() {
        DocumentIndexEntry.PATH_TO_DOCUMENT_INDEX = "src/test/data/testDocIndex";
        DocumentIndexEntry.memOffset = 0;
    }

    /**
     * function to write a summarization of the most important data about a document index entry as plain text in the debug file
     * @param path: path of the file where to write
     */
    public void debugWriteToDisk(String path) {
        FileUtils.createDirectory("data/debug");
        FileUtils.createIfNotExists("data/debug/"+path);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/debug/"+path, true));
            writer.write(this.toString()+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof DocumentIndexEntry de)) {
            return false;
        }

        return de.getDocid() == this.docid && de.getDocLen() == this.getDocLen() && de.getPid().equals(this.getPid());
    }

    /**
     * Reset the memory offset for testing
     */
    public static void resetOffset() {
        memOffset = 0;
    }
}
