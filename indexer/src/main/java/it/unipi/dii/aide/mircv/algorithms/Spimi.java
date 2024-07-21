package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.*;
import it.unipi.dii.aide.mircv.config.CollectionSize;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.preprocess.Preprocesser;
import it.unipi.dii.aide.mircv.utils.FileUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Spimi {
    // Path to the file on Disk storing processed collections
    private static final String PATH_TO_COLLECTION = ConfigurationParams.getRawCollectionPath();

    /**
     * path to the file on the disk storing the compressed collection
     */
    protected static final String PATH_TO_COMPRESSED_COLLECTION = ConfigurationParams.getCompressedCollectionPath();

    /*
    path to the file on the disk storing the partial vocabulary
    */
    private static final String PATH_TO_PARTIAL_VOCABULARY = ConfigurationParams.getPartialVocabularyDir() + ConfigurationParams.getVocabularyFilename();

    /*
   path to the file on the disk storing the partial frequencies of the posting list
   */
    private static final String PATH_TO_PARTIAL_FREQUENCIES = ConfigurationParams.getFrequencyDir() + ConfigurationParams.getFrequencyFileName();

    /*
    path to the file on the disk storing the partial docids of the posting list
    */

    private static final String PATH_TO_PARTIAL_DOCID = ConfigurationParams.getDocidsDir() + ConfigurationParams.getDocidsFileName();

    // Counter for the partial indexes created
    private static int numIndex = 0;

    /*
    counts the number of partial indexes to write
     */
    private static long numPostings = 0;


    /**
     * @param compressed  flag for compressed reading
     * @return buffer reader
     * initializes the buffer from which the entries are read
     * */
    private static BufferedReader initializeBuffer(boolean compressed) throws IOException{
            if (compressed) {
                TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(PATH_TO_COMPRESSED_COLLECTION)));
                tarIn.getNextTarEntry();
                return new BufferedReader(new InputStreamReader(tarIn, StandardCharsets.UTF_8));
        }
        return Files.newBufferedReader(Paths.get(PATH_TO_COLLECTION), StandardCharsets.UTF_8);
    }

    /**
     * deletes directories containing partial data structures and document Index file
     */
    private static void rollback(){
        FileUtils.deleteDirectory(ConfigurationParams.getDocidsDir());
        FileUtils.deleteDirectory(ConfigurationParams.getFrequencyDir());
        FileUtils.deleteDirectory(ConfigurationParams.getPartialVocabularyDir());
        FileUtils.removeFile(ConfigurationParams.getDocumentIndexPath());
    }

    /**
     * writes the partial index on file
     * @param index: partial index that must be saved onto file
     * @return
     */
    private static boolean saveIndexToDisk(HashMap<String, PostingList> index, boolean debugMode) {
        System.out.println("Saving index:" + numIndex +  " of size: " +index.size()+ " to disk");

        // If index is empty, then there is nothing to write.
        if (index.isEmpty()) {
            System.out.println("Index is empty, nothing to write to disk");
            return true;
        }

        // Sort the index in lexicographic order
        index = index.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // Try to open a file channel of the inverted index
        try (
                FileChannel docsFChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_PARTIAL_DOCID + "_" + numIndex),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);
                FileChannel freqFChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_PARTIAL_FREQUENCIES + "_" + numIndex),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);
                FileChannel vocabFChannel = (FileChannel) Files.newByteChannel(
                        Paths.get(PATH_TO_PARTIAL_VOCABULARY + "_" + numIndex),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);
        ) {
            // Write the index to disk
            MappedByteBuffer docsBuffer = docsFChannel.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);

            MappedByteBuffer freqsBuffer = freqFChannel.map(FileChannel.MapMode.READ_WRITE, 0, numPostings * 4L);

            long vocOffset = 0;
            // Check if mappedBytesBuffers are correctly instantiated
            if (docsBuffer != null || freqsBuffer != null) {
                for (PostingList list : index.values()) {
                    //create vocabulary entry
                    VocabularyEntry vocEntry = new VocabularyEntry(list.getTerm());
                    vocEntry.setMemoryOffset(docsBuffer.position());
                    vocEntry.setFrequencyOffset(docsBuffer.position());

                    // write postings to file
                    for (Posting posting : list.getPostings()) {
                        // encode docid
                        docsBuffer.putInt(posting.getDocid());
                        // encode freq
                        freqsBuffer.putInt(posting.getFrequency());
                    }
                    vocEntry.updateStatistics(list);
                    vocEntry.setBM25Dl(list.getBM25Dl());
                    vocEntry.setBM25Tf(list.getBM25tf());
                    vocEntry.setDocidSize((int) (numPostings*4));
                    vocEntry.setFrequencySize((int) (numPostings*4));

                    vocOffset = vocEntry.writeEntryToDisk(vocOffset, vocabFChannel);
                    if(debugMode){
                        list.debugSaveToDisk("partialDOCIDS_"+numIndex+".txt", "partialFREQS_"+numIndex+".txt", (int) numPostings);
                        vocEntry.debugSaveToDisk("partialVOC_"+numIndex+".txt");
                    }
                }
            }

            // Update the number of indexes
            numIndex++;
            numPostings = 0;
            return true;
        } catch (InvalidPathException e) {
            System.out.println("Invalid path exception " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println("IO exception " + e.getMessage());
            return false;
        }
    }

    /**
     * Function that searches for a given docid in a posting list
     * If the document already exists, updates the frequency for that document
     * Else it creates a new pair of (docid, freq) with freq = 1 and added to postingList
     * @param docid: docid of a document
     * @param postingList: postingList of a given term
     */
    private static void updateOrAddPosting(int docid, PostingList postingList){
        if(postingList.getPostings().size() > 0) {
            // Last document inserted
            Posting lastPosting = postingList.getPostings().get(postingList.getPostings().size() - 1);
            if (docid == lastPosting.getDocid()) {
                lastPosting.setFrequency(lastPosting.getFrequency() + 1);
                return;
            }
        }
        // The document has not been processed
        postingList.getPostings().add(new Posting(docid, 1));

        // Increment the number of postings
        numPostings++;
    }

    /**
     * Function that executes the SPIMI algorithm
     * @param compressedReadingEnable flag enabling reading from compressed file and stemming if true
     * @param debug flag enabling debug mode
     * @return the number of indexes created
     */
    public static int executeSpimi(boolean compressedReadingEnable, boolean debug) {
        numIndex = 0;
        DocumentIndexEntry.resetOffset();

        try(BufferedReader reader = initializeBuffer(compressedReadingEnable)) {
            boolean allDocumentsProcessed = false; // flag to check if all documents are processed

            int docid = 1; // assignment for doc ids in incremental value
            int docsLen = 0; // length of the documents
            boolean writeSuccess; // flag to check if the write was successful

            // Build the index until memory is available with memory threshold
            long MEMORY_THRESHOLD = Runtime.getRuntime().totalMemory() * 20 / 100; // 20% of total memory

            String[] split;
            while(!allDocumentsProcessed) {
                HashMap<String, PostingList> index = new HashMap<>(); // create a new index

                while(Runtime.getRuntime().freeMemory() > MEMORY_THRESHOLD){
                    String line;

                    // If we reach the end of the file
                    if((line = reader.readLine()) == null){
                        System.out.println("All documents were processed");
                        allDocumentsProcessed = true;
                        break;
                    }

                    // If the line is empty, process next line
                    if (line.isBlank())
                        continue;

                    // Split the line into pid and tokens
                    split = line.split("\t");

                    // Create a new document
                    TextDocument document = new TextDocument(split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));

                    // Perform text preprocessing on the document
                    ProcessedDocument processedDocument = Preprocesser.preprocessDocument(document);

                    // Check If the body is empty
                    if(processedDocument.getTokens().isEmpty())
                        continue;

                    // Update the number of documents
                    int documentLength = processedDocument.getTokens().size();

                    DocumentIndexEntry documentIndexEntry = new DocumentIndexEntry(
                            processedDocument.getPid(),
                            docid,
                            documentLength
                    );

                    // update with length of the new docs
                    docsLen += documentIndexEntry.getDocLen();

                    // Add the document to the index
                    documentIndexEntry.writeToDisk();

                    // Check if debug flag
                    if(debug)
                        documentIndexEntry.debugWriteToDisk("debugDocIndex.txt");

                    // For each term in the document
                    for (String term : processedDocument.getTokens()) {
                        if(term.isBlank())
                            continue;

                        PostingList postingList;

                        // If the term is not present in the index
                        if (!index.containsKey(term)) {
                            postingList = new PostingList(term);
                            index.put(term, postingList);
                        } else {
                            // Get the posting list
                            postingList = index.get(term);
                        }

                        // Update or add the posting
                        updateOrAddPosting(docid, postingList);
                        postingList.updateBM25Params(documentLength, postingList.getPostings().size());
                    }
                    docid++;
                    if((docid % 1000000) == 0)
                        System.out.println("Processed " + docid + " documents");
                }

                // Either if there is no memory available or all documents were read, flush partial index onto disk
                writeSuccess = saveIndexToDisk(index, debug);

                // If the write was not successful, rollback
                if(!writeSuccess){
                    System.out.println("Error while writing the index to disk");
                    rollback();
                    return -1;
                }
                index.clear();
            }
            // Update the number of indexes and save to disk
            if(!CollectionSize.updateCollectionSize(docid -1) || !CollectionSize.updateTotalDocLen(docsLen)){
                System.out.println("Error while updating the collection size");
                return 0;
            }

            return numIndex;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}