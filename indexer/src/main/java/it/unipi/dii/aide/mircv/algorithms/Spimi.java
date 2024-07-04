package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.dto.ProcessedDocumentDTO;
import it.unipi.dii.aide.mircv.utils.CollectionStats;
import it.unipi.dii.aide.mircv.utils.FileUtils;
import it.unipi.dii.aide.mircv.utils.Utility;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Spimi {
    // Path to the file on Disk storing processed collections
    private static final String PATH_TO_DOCUMENTS = ConfigurationParams.getProcessedCollectionPath();

    // Path to the file on the Disk for storing partial indexes
    private static final String PATH_BASE_TO_INDEX = ConfigurationParams.getPartialIndexPath();

    // Path to the file on Disk for storing document index
    private static final String PATH_TO_DOCUMENT_INDEX = ConfigurationParams.getDocumentIndexPath();

    // Memory alloc to be kept free
    private static final long MEMORY_THRESHOLD = 80000000;

    // Hashmap to store the partial Inverted index
    //private static HashMap<String, ArrayList<MutablePair<Integer, Integer>>> index = new HashMap<>();

    // Counter for the partial indexes created
    private static int num_index = 0;

    /**
     * Function to flush partial index onto disk
     * and cleans the index data structure so it can be re-used
     *
     * @param index: the partial index to be saved on disk
     *             and cleaned for further use
     */
    private static void saveIndexToDisk(HashMap<String, PostingList> index) {
        // If index is empty, then there is nothing to write.
        if (index.isEmpty())
            return;

        // Sort the index in lexicographic order
        index = index.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // Write the index to disk
        try (DB db = DBMaker.fileDB(PATH_BASE_TO_INDEX + num_index + ".db").fileChannelEnable().fileMmapEnable().make()) {
             ArrayList<PostingList> partialIndex = (ArrayList<PostingList>) db.indexTreeList("index_" + num_index, Serializer.JAVA).createOrOpen();
            partialIndex.addAll(index.values());

            num_index++;        // update the number of partial inverted indexes
        } catch (Exception e) {
            e.printStackTrace();
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
        boolean found = false;

        // Iterate on each postingList
        for(Map.Entry<Integer, Integer> posting: postingList.getPostings()){
            // If the docid is found, update the frequency
            if(docid == posting.getKey()){
                posting.setValue(posting.getValue() + 1);
                found = true;
            }
        }

        if(!found)
            postingList.getPostings().add(new AbstractMap.SimpleEntry<>(docid, 1));
    }



    public static void spimi(){
        // Use the BufferedReader to read processed docs line by line
        try (DB db = DBMaker.fileDB(PATH_TO_DOCUMENTS).fileChannelEnable().fileMmapEnable().make();
            DB docIndexDB = DBMaker.fileDB(PATH_TO_DOCUMENT_INDEX).fileChannelEnable().fileMmapEnable().make();
            HTreeMap collection = db.hashMap("processedCollection")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();

            HTreeMap<Integer, String> docIndex = docIndexDB.hashMap("docIndex")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.STRING)
                    .createOrOpen();
            ) {
                boolean allDocumentsProcessed = false;

                int docid = 0;      // count for doc ids

                while(!allDocumentsProcessed) {
                    // Build the index until memory is available with memory threshold
                    HashMap<String, PostingList> index = new HashMap<>();;

                    while(Runtime.getRuntime().freeMemory() > MEMORY_THRESHOLD){
                    // Build the index until memory is available with memory threshold
                    //System.out.println(Runtime.getRuntime().freeMemory());

                        Iterator<String> keyIterator = (Iterator<String>) collection.keySet().iterator();
                        if(!keyIterator.hasNext()){
                            // If all documents are processed
                            allDocumentsProcessed = true;
                            break;
                        }
                        // Get the next document from the collection
                        ProcessedDocumentDTO document = new ProcessedDocumentDTO();
                        String pid = keyIterator.next();
                        document.setPid(pid);
                        document.setTokens((ArrayList<String>) collection.get(pid));

                        // Add the document to the document index in the format docid -> pid, doc_length \n
                        String entry = docid++ + "\t" + pid + "," + document.getTokens().size() + "\n";
                        docIndex.put(docid, entry);
                        System.out.println("Docid: " + docid + " PID: " + pid);
                        CollectionStats.addDocument();
                        for(String term: document.getTokens()){
                            PostingList posting;
                            // If the term is already present in the index
                            if(!index.containsKey(term)){
                                // Create a new posting list
                                posting = new PostingList(term);
                                index.put(term, posting);
                            }else{
                                // Get the posting list for the term from the index
                                posting = index.get(term);
                            }
                            // Insert/Update the posting list with the docid
                            updateOrAddPosting(docid, posting);
                        }
                        saveIndexToDisk(index);
                    }
                }
            Utility.setNumIndexes(num_index);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}