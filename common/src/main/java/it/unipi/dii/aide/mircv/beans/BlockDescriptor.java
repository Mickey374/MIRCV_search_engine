package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.config.Flags;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * This class represents a block descriptor used to implement skipping.
 */
public class BlockDescriptor {

    /**
     * Starting byte in the block of the docid file of the Inverted index
     */
    private long docidOffset;

    /**
     * Starting size of the block in the docid file
     */
    private int docidSize;

    /**
     * Starting byte in the block of the frequency file of the Inverted index
     */
    private long freqOffset;

    /**
     * Starting size of the block in the frequency file
     */
    private int freqSize;

    /**
     * Max docid in the block
     */
    private int maxDocid;

    /**
     * Number of elements in the block
     */
    private int numPostings;

    /**
     * Number of bytes used to store the block descriptor: 4 integers (16 bytes) + 2 long (8 bytes) = 32 bytes
     */
    public static final int BLOCK_DESCRIPTOR_ENTRY_BYTES = 4 * 4 + 2 * 8;

    /**
     * Memory offset reached by the block descriptor
     */
    private static long memoryOffset = 0;

    /**
     * Path to docid file of Inverted index
     */
    private static String INVERTED_INDEX_DOCS = ConfigurationParams.getInvertedIndexDocs();

    /**
     * Path to the frequency file of Inverted index
     */
    private static String INVERTED_INDEX_FREQS = ConfigurationParams.getInvertedIndexFreqs();

    public static long getMemoryOffset() {
        return memoryOffset;
    }

    public void setDocidOffset(long docidOffset) {
        this.docidOffset = docidOffset;
    }

    public void setDocidSize(int docidSize) {
        this.docidSize = docidSize;
    }

    public void setFreqOffset(long freqOffset) {
        this.freqOffset = freqOffset;
    }

    public void setFreqSize(int freqSize) {
        this.freqSize = freqSize;
    }

    public void setMaxDocid(int maxDocid) {
        this.maxDocid = maxDocid;
    }

    public void setNumPostings(int numPostings) {
        this.numPostings = numPostings;
    }

    public int getMaxDocid() {
        return maxDocid;
    }

    /**
     * Method that saves the block descriptor to the disk
     * @param fChannelDocid FileChannel of the docid file
     * @return true if the block descriptor has been saved correctly, false otherwise
     */
    public boolean saveBlockDescriptorOnDisk(FileChannel fChannelDocid) {
        try {
            MappedByteBuffer buffer = fChannelDocid.map(FileChannel.MapMode.READ_WRITE, memoryOffset, BLOCK_DESCRIPTOR_ENTRY_BYTES);

            if(buffer != null) {
                buffer.putInt(docidSize);
                buffer.putInt(numPostings);
                buffer.putInt(maxDocid);
                buffer.putLong(docidOffset);
                buffer.putLong(freqOffset);
                buffer.putInt(freqSize);

                memoryOffset += BLOCK_DESCRIPTOR_ENTRY_BYTES;
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method that gets block postings from file using compressed mode or not
     * @return arraylist of postings
     */
    public ArrayList<Posting> getBlockPostings() {
        try(FileChannel docsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_DOCS),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE
        );
        FileChannel freqsFChan = (FileChannel) Files.newByteChannel(Paths.get(INVERTED_INDEX_FREQS),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE);
        ) {
            // Instantiate the arraylist of postings for integer list of docids and frequencies
            MappedByteBuffer docBuffer = docsFChan.map(FileChannel.MapMode.READ_ONLY, docidOffset, docidSize);
            MappedByteBuffer freqBuffer = freqsFChan.map(FileChannel.MapMode.READ_ONLY, freqOffset, freqSize);

            if(docBuffer == null || freqBuffer == null) {
                return null;
            }

            ArrayList<Posting> block = new ArrayList<>();

            if(Flags.isCompressionEnabled()){
                // If compression is enabled, use the appropriate decompressor
                byte[] compressedDocids = new byte[docidSize];
                byte[] compressedFreqs = new byte[freqSize];

                // Read the compressed docids and frequencies
                docBuffer.get(compressedDocids, 0, docidSize);
                freqBuffer.get(compressedFreqs, 0, freqSize);

                // Decompress the docids and frequencies
                int[] decompressedDocids = VariableByteCompressor.integerArrayDecompression(compressedDocids, numPostings);
                int[] decompressedFreqs = UnaryCompressor.integerArrayDecompression(compressedFreqs, numPostings);

                // Create the postings
                for(int i = 0; i < numPostings; i++) {
                    block.add(new Posting(decompressedDocids[i], decompressedFreqs[i]));
                }
            } else {
                // If compression is not enabled, read the docids and frequencies directly
                for(int i = 0; i < numPostings; i++) {
                    block.add(new Posting(docBuffer.getInt(), freqBuffer.getInt()));
                }
            }
            return block;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "BlockDescriptor Info: \n{" +
                "docidOffset = " + docidOffset +
                ", docidSize = " + docidSize +
                ", freqOffset = " + freqOffset +
                ", freqSize = " + freqSize +
                ", maxDocid = " + maxDocid +
                ", numPostings = " + numPostings +
                '}';
    }

    /**
     * Method that sets the path to the docid file of the Inverted index
     * @param invertedIndexFreqs path to the docid file
     */
    public static void setInvertedIndexFreqs(String invertedIndexFreqs) {
        INVERTED_INDEX_FREQS = invertedIndexFreqs;
    }

    /**
     * Method that sets the path to the frequency file of the Inverted index
     * @param invertedIndexDocs path to the frequency file
     */
    public static void setInvertedIndexDocs(String invertedIndexDocs) {
        INVERTED_INDEX_DOCS = invertedIndexDocs;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(obj== null || getClass() != obj.getClass()) {
            return false;
        }

        BlockDescriptor bd = (BlockDescriptor) obj;

        return docidOffset == bd.docidOffset &&
                docidSize == bd.docidSize &&
                freqOffset == bd.freqOffset &&
                freqSize == bd.freqSize &&
                maxDocid == bd.maxDocid &&
                numPostings == bd.numPostings;
    }

    /**
     * Method that sets the memory offset
     * @param memoryOffset memory offset
     */
    public static void setMemoryOffset(long memoryOffset) {
        BlockDescriptor.memoryOffset = memoryOffset;
    }
}
