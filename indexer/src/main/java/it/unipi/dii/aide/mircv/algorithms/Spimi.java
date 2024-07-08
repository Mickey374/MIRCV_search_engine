package it.unipi.dii.aide.mircv.algorithms;

import it.unipi.dii.aide.mircv.beans.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.dto.ProcessedDocumentDTO;
import it.unipi.dii.aide.mircv.utils.CollectionStats;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.*;
import java.util.stream.Collectors;

public class Spimi {
    // Path to the file on Disk storing processed collections
    private static final String PATH_TO_DOCUMENTS = ConfigurationParams.getProcessedCollectionPath();

    // Path to the file on the Disk for storing partial indexes
    private static final String PATH_PARTIAL_INDEX = ConfigurationParams.getPartialIndexPath();

    // Path to the file on Disk for storing document index
    private static final String PATH_TO_DOCUMENT_INDEX = ConfigurationParams.getDocumentIndexPath();

    // Memory alloc to be kept free
    private static final long MEMORY_THRESHOLD = 80000000;

    // Counter for the partial indexes created
    private static int numIndex = 0;

    /**
     * Function to flush partial index onto disk
     * and cleans the index data structure so it can be re-used
     *
     * @param index: the partial index to be saved on disk
     *             and cleaned for further use
     */
    private static void saveIndexToDisk(HashMap<String, PostingList> index, DB db) {
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
        //try (DB db = DBMaker.fileDB(PATH_BASE_TO_INDEX + num_index + ".db").fileChannelEnable().fileMmapEnable().make()) {
        List<PostingList> partialIndex = (List<PostingList>) db.indexTreeList("index_" + numIndex, Serializer.JAVA).createOrOpen();
        partialIndex.addAll(index.values());

        numIndex++;        // update the number of partial inverted indexes
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
            // Iterate and stop search if we find higher value than docid
            if (docid > posting.getKey())
                break;

            // If the docid is found, update the frequency
            if(docid == posting.getKey()){
                posting.setValue(posting.getValue() + 1);
                found = true;
                break;
            }
        }
        // If the docid is not found, add a new pair to the postingList
        if(!found)
            postingList.getPostings().add(new AbstractMap.SimpleEntry<>(docid, 1));
    }


    public static void read() {
        // use the DBMaker to create a DB object and open the collection
        DB db = DBMaker.fileDB(PATH_PARTIAL_INDEX).make();

        // use the DB object to open the Hashmap
        List<PostingList> partialIndex = (List<PostingList>) db.indexTreeList("index_" + 0, Serializer.JAVA).createOrOpen();

        // iterate over the partial index and read from the Map
        Iterator<PostingList> keys = partialIndex.stream().iterator();
        while (keys.hasNext()) {
            System.out.println(keys.next());
        }

        // close the DB
        db.close();
    }

    public static void readDocIndex() {
        // use the DBMaker to create a DB object and open the collection
        DB db = DBMaker.fileDB(PATH_TO_DOCUMENT_INDEX).make();

        // use the DB object to open the Hashmap
        List<DocumentIndexEntry> docIndex =(List<DocumentIndexEntry>) db.indexTreeList("docIndex", Serializer.JAVA).createOrOpen();

        System.out.println(docIndex.size());

        // read from map
        Iterator<DocumentIndexEntry> keys = docIndex.stream().iterator();
        while (keys.hasNext()) {
            System.out.println(keys.next());
        }
        // close the DB
        db.close();
    }

    public static int executeSpimi() {
        try(DB db = DBMaker.fileDB(PATH_TO_DOCUMENTS).fileChannelEnable().fileMmapEnable().make();  //fileDB for processed documents
            DB partialIndex = DBMaker.fileDB(PATH_PARTIAL_INDEX).fileChannelEnable().fileMmapEnable().make(); //fileDB for partial indexes
            DB docIndexDB = DBMaker.fileDB(PATH_TO_DOCUMENT_INDEX).fileChannelEnable().fileMmapEnable().make();
            HTreeMap collection = db.hashMap("processedCollection")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        ){
            boolean allDocumentsProcessed = false; // flag to check if all documents are processed

            // List containing all document indexes that must be written to disk
            List<DocumentIndexEntry> docIndex = (List<DocumentIndexEntry>) docIndexDB.indexTreeList("docIndex", Serializer.JAVA).createOrOpen();

            int docid = 0; // count for doc ids

            // Build the index until memory is available with memory threshold
            long MEMORY_THRESHOLD = Runtime.getRuntime().totalMemory() * 20 / 100; // 20% of total memory

            Iterator<String> keyIterator = (Iterator<String>) collection.keySet().iterator();

            while(!allDocumentsProcessed) {
                HashMap<String, PostingList> index = new HashMap<>(); // create a new index

                while(Runtime.getRuntime().freeMemory() > MEMORY_THRESHOLD){
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

                    // Add the document to the document index in the format pid -> docid, doc_length \n
                    DocumentIndexEntry entry = new DocumentIndexEntry(pid, docid++, document.getTokens().size());
                    docIndex.add(entry);
                    System.out.println("PID: " + pid + " DocID: " + docid);
                    CollectionStats.addDocument();

                    for(String term: document.getTokens()){
                        PostingList posting;
                        // If the term is already present in the index
                        if(!index.containsKey(term)){
                            // Create a new posting list
                            posting = new PostingList(term);
                            // Add the posting list to the index
                            index.put(term, posting);
                        }else{
                            // Get the posting list for the term from the index
                            posting = index.get(term);
                        }
                        // Insert/Update the posting list with the docid
                        updateOrAddPosting(docid, posting);
                    }
                }
                // Save the partial index to disk
                saveIndexToDisk(index, partialIndex);
            }
            // TODO: Decide if the num_indexes can be the return of Spimi and pass it to merger

            return numIndex;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}