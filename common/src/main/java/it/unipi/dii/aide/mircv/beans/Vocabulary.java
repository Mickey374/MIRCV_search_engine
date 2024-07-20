package it.unipi.dii.aide.mircv.beans;

import it.unipi.dii.aide.mircv.config.CollectionSize;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;

import org.junit.platform.commons.util.LruCache;
import java.util.LinkedHashMap;

/**
 * The vocabulary class, a singleton object that contains the vocabulary of the collection.
 */
public class Vocabulary extends LinkedHashMap<String, VocabularyEntry> {
    /**
     * The instance of the Singleton object
     */
    private static Vocabulary instance = null;

    /**
     * The cache for the vocabulary
     */
    private final static LruCache<String, VocabularyEntry> entries = new LruCache<>(1000);

    /**
     * Path to the file storing the vocabulary
     */
    protected static String PATH_TO_VOCABULARY = ConfigurationParams.getVocabularyPath();

    /**
     * The default constructor with 0 params
     */
    private Vocabulary(){}

    /**
     * Get the instance of the Singleton object
     * @return the instance of the Singleton object
     */
    public static Vocabulary getInstance() {
        if (instance == null) {
            instance = new Vocabulary();
        }
        return instance;
    }

    /**
     * get idf of a term
     * @param term term of which we wnat to get the idf
     * @return idf of such term
     */
    public double getIdf(String term) {
        return getEntry(term).getIdf();
    }

    /**
     * gets vocabulary entry of a given term
     * @param term term of which we want to get its vocabulary entry
     * @return vocabulary entry of such term
     */
    public VocabularyEntry getEntry(String term) {
        //if term is cached, return its vocabulary entry
        if (entries.containsKey(term))
            return entries.get(term);

        //if term is not cached, load it from disk
        VocabularyEntry entry =  findEntry(term);

        //if entry is not null, cache it and return it
        if (entry != null)
            entries.put(term, entry);

        return entry;
    }

    /**
     *  used for testing purposes only to read from disk
     */
    public boolean readFromDisk() {
        long position = 0;

        //for each term in the vocabulary, read it from disk
        while(position >= 0) {
            VocabularyEntry entry = new VocabularyEntry();
            position = entry.readFromDisk(position, PATH_TO_VOCABULARY);

            //if entry is not null, add it to the vocabulary
            if (position == 0)
                return true;

            if(entry.getTerm() == null)
                return true;

            // Populate the vocabulary with the entry
            this.put(entry.getTerm(), entry);
            entries.put(entry.getTerm(), entry);
        }
        //if position == -1 an error occurred during reading
        return position != -1;
    }

    /**
     * retrieves the vocabulary entry of a given term from disk
     * @param term: term of which we want vocabulary entry
     * @return the vocabulary entry of given term, null if term is not in vocabulary
     **/
    public VocabularyEntry findEntry(String term) {
        // Entry to be returned
        VocabularyEntry entry = new VocabularyEntry();

        long start = 0;                                             // Start position of the entry
        long end = CollectionSize.getVocabularySize() -1;           // End position of the entry
        long mid;                                                   // Index of the elem of vocabulary to read
        String key;                                                 // elem of vocabulary read
        long entrySize = VocabularyEntry.ENTRY_SIZE;                // Size of the entry

        // Binary search to find the term in the vocabulary
        while (start <= end) {
            mid = start + (end - start) / 2;

            // Read the term from disk
            entry.readFromDisk(mid * entrySize, PATH_TO_VOCABULARY);
            key = entry.getTerm();

            if (key == null) {
                return null;
            }
            if (key.equals(term)) {
                return entry;
            }

            //update search portion parameters
            if (term.compareTo(key) > 0) {
                start = mid + 1;
                continue;
            }

            end = mid - 1;
        }
        return null;
    }

    /** needed for testing purposes
     * @param path: path to be set
     */
    public static void setPathToVocabulary(String path) {
        PATH_TO_VOCABULARY = path;
    }

    /**
     * Testing purposes: Clear the cache
     */
    public static void clearCache() {
        entries.clear();
    }

    /**
     * needed for testing purposes: unset the instance
     */
    public static void unsetInstance(){
        instance = null;
    }
}