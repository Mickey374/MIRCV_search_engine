package queryProcessing;

import it.unipi.dii.aide.mircv.beans.*;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.preprocess.Preprocesser;
import it.unipi.dii.aide.mircv.config.Flags;

import java.util.*;
import java.io.File;
import java.util.stream.Collectors;

/**
 * QueryProcesser class
 */
public class QueryProcesser {
    /**
     * Vocabulary instance
     */
    private static final Vocabulary vocabulary = Vocabulary.getInstance();

    /**
     * DocumentIndex instance
     */
    private static final DocumentIndex documentIndex = DocumentIndex.getInstance();

    /**
     * Path for Storing Inverted Index docids
     */
    private static final String INVERTED_INDEX_DOCIDS_PATH = ConfigurationParams.getInvertedIndexDocs();

    /**
     * Path for Storing Inverted Index tf
     */
    private static final String INVERTED_INDEX_FREQS_PATH = ConfigurationParams.getInvertedIndexFreqs();


    /**
     * load from disk the posting lists of the query tokens
     * @param query         the query document
     * @param isConjunctive specifies if the query has to be processed in conjunctive mode
     * @return the list of the query terms' posting lists
     */
    public static ArrayList<PostingList> getQueryPostings(ProcessedDocument query, boolean isConjunctive){
        // ArrayList with all the posting lists
        ArrayList<PostingList> queryPostings = new ArrayList<>();

        ArrayList<String> queryTerms = query.getTokens();
        //remove duplicates
        queryTerms = (ArrayList<String>) queryTerms.stream()
                .distinct()
                .collect(Collectors.toList());

        for(String queryTerm: queryTerms){
            VocabularyEntry entry = vocabulary.getEntry(queryTerm);

            if(entry == null){
                //if query is in conjunctive mode and a query term is not present
                //in the collection, no document will satisfy the request
                if(isConjunctive)
                    return null;

                continue;
            }
            vocabulary.put(queryTerm, entry);
            queryPostings.add(new PostingList(entry.getTerm()));
        }
        return queryPostings;
    }

    /**
     * Lookups in the document index to retrieve pids of the top-k documents
     * @param priorityQueue The top scored documents
     * @param k number of documents to return
     * @return the ordered array of document pids
     */
    public static String[] lookupPid(PriorityQueue<Map.Entry<Double, Integer>> priorityQueue, int k) {
        String[] topKDocuments = new String[k];
        int i = priorityQueue.size() - 1;
        while (i >= 0) {
            if (priorityQueue.peek() == null)
                break;
            topKDocuments[i] = documentIndex.getPid(priorityQueue.poll().getValue());
            i--;
        }
        return topKDocuments;
    }

    /**
     * Processes a query, computing the score for each document and returning the top-k documents
     * @param query The query string
     * @param k number of documents to retrieve
     * @param isConjunctive specifies if the query is conjunctive
     * @param scoringFunction specifies which scoring function should be used to process the query ("tfidf" or "bm25")
     * @return an array with the top-k document pids
     */
    public static String[] processQuery(String query, int k, boolean isConjunctive, String scoringFunction) {
        // Create a new TextDocument object with the query
        TextDocument queryDoc = new TextDocument("query", query);

        // Perform text processing on the document
        ProcessedDocument processedQuery = Preprocesser.preprocessDocument(queryDoc);

        // Perform the query processing
        ArrayList<PostingList> queryPostings = getQueryPostings(processedQuery, isConjunctive);
        if (queryPostings == null || queryPostings.isEmpty())
            return null;

        PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;
        if (!Flags.isMaxScoreEnabled())
            priorityQueue = DAAT.scoreQuery(queryPostings, true, k, scoringFunction);
        else
            priorityQueue = MaxScore.scoreQuery(queryPostings, k, scoringFunction, isConjunctive);

        return lookupPid(priorityQueue, k);
    }

    /**
     * checks if the data structures needed for query processing were correctly created
     * @return boolean
     */
    public static boolean setupProcesser() {
        //initialize flags
        if (!Flags.initializeFlags())
            return false;

        //check if document index exists. If not the setup failed
        if (!new File(INVERTED_INDEX_DOCIDS_PATH).exists() || !new File(INVERTED_INDEX_FREQS_PATH).exists())
            return false;

        // load the document index
        if (!documentIndex.loadFromDisk())
            return false;

        //check if document index contains entries. If not the setup failed
        return !documentIndex.isEmpty();
    }
}
