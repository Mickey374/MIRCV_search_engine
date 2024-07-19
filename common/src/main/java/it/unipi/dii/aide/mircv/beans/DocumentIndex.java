package it.unipi.dii.aide.mircv.beans;


import it.unipi.dii.aide.mircv.config.CollectionSize;
import java.util.LinkedHashMap;


public class DocumentIndex extends LinkedHashMap<Integer, DocumentIndexEntry> {
    /**
     * Instance of the Singleton object
     */
    private static DocumentIndex instance = null;

    /**
     * Default constructor
     */
    private DocumentIndex() {
    }

    /**
     * Get the instance of the Singleton object
     * @return the instance of the Singleton object
     */
    public static DocumentIndex getInstance() {
        if (instance == null) {
            instance = new DocumentIndex();
        }
        return instance;
    }

    /**
     * Lookup method on the document index
     * @param docId the document id
     * @return the pid of the document
     */
    public String getPid(int docId) {
        return this.get(docId).getPid();
    }

    /**
     * Lookup method on the document index
     * @param docId the document id
     * @return the length of the document
     */
    public int getLength(int docId) {
        return this.get(docId).getDocLen();
    }

    /**
     * Loads the document index from disk
     * @return true if fetch is successful
     */
    public boolean loadFromDisk() {
        // retrieve the document index from disk
        long numDocs = CollectionSize.getCollectionSize();

        // Retrieve the size of a single entry
        final int ENTRY_SIZE = DocumentIndexEntry.getEntrySize();

        // For each document, read the entry from disk
        for (int i = 0; i < numDocs; i++) {
            // Create a new entry
            DocumentIndexEntry newEntry = new DocumentIndexEntry();
            if(newEntry.readFromDisk((long) i * ENTRY_SIZE)) {
                // Insert the entry into the document index
                this.put(newEntry.getDocid(), newEntry);
            } else {
                return false;
            }
        }
        return true;
    }
}
