package queryProcessing;

import it.unipi.dii.aide.mircv.beans.*;
import java.util.*;

/**
 * DAAT class
 */
public class DAAT {
    /**
     * Opens the lists to scan during the score
     * @param queryTerms the query terms
     */
    public static void initialize(ArrayList<PostingList> queryTerms) {
        for (PostingList postingList : queryTerms) {
            postingList.openPostingList();
            postingList.next();
        }
    }

    /**
     * Closes all the posting lists
     * @param queryTerms the query terms
     */
    public static void cleanUp(ArrayList<PostingList> queryTerms) {
        for (PostingList postingList : queryTerms)
            postingList.closePostingList();
    }

    /** method to move the iterators of postingsToScore to the given docid
     * @param docidToProcess: docid to which the iterators must be moved to
     * @return -1 if there is at least a list for which there is no docid >= docidToProcess
     */
    public static int nextGEQ(int docidToProcess, ArrayList<PostingList> postingsToScore) {
        int nextGEQ = docidToProcess;

        for(int i=0; i<postingsToScore.size(); i++) {
            // Get the i-th posting list
            PostingList currPostingList = postingsToScore.get(i);

            if (currPostingList != null) {
                Posting pointedPosting = currPostingList.getCurrentPosting();

                // If the current posting is null, skip to the next one
                if (pointedPosting == null) {
                    return -1;
                }

                // If the current posting is less than the nextGEQ, skip to the next one
                if (pointedPosting.getDocid() < nextGEQ) {
                    pointedPosting = currPostingList.nextGEQ(nextGEQ);
                    // check if in the current posting list there is no docid >= docidToProcess to be processed
                    if (pointedPosting == null) {
                        return -1;
                    }

                    if (pointedPosting.getDocid() == nextGEQ)
                        continue;
                }

                // check if in the current posting list is not present docidToProcess, but it is present a docid >=
                if (pointedPosting.getDocid() > nextGEQ) {
                    // the current docid will be the candidate next docid to be processed

                    // set nextGEQ to new value
                    nextGEQ = pointedPosting.getDocid();
                    i = -1;
                }
            }
        }
        return nextGEQ;
    }


    /** method to find next document to be processed among all the postings to be scored
     * @return
     * - if query mode is DISJUNCTIVE, return the minimum docid among all the first docids in the posting lists of the array, -1 if not possible
     * - else, query mode is CONJUNCTIVE, return the maximum docid among all the first docids in the posting lists of the array,
     *   it returns -1 if the maximum docid is not present in all the posting lists to be scored
     * */
    public static int nextDocToProcess(boolean isConjuctive, ArrayList<PostingList> postingsToScore) {
        int docidToProcess = -1;

        // Go through all posting lists of other terms
        for (PostingList currPostingList : postingsToScore) {
            // If the current posting is null, skip to the next one
            if (currPostingList != null && currPostingList.getCurrentPosting() != null) {
                // Get the current posting
                int pointedDocid = currPostingList.getCurrentPosting().getDocid();

                // If the current posting is less than the nextGEQ, skip to the next one
                if (!isConjuctive) {
                    // DISJUNCTIVE Mode
                    if (docidToProcess == -1 || pointedDocid < docidToProcess)
                        docidToProcess = pointedDocid;
                } else {
                    // CONJUNCTIVE Mode
                    if (pointedDocid > docidToProcess)
                        docidToProcess = pointedDocid;
                }
            }
        }
        if (isConjuctive)
            return nextGEQ(docidToProcess, postingsToScore);
        else
            return docidToProcess;
    }


    /**
     * method to compute the IDF score of a particular document identified by docid
     * @param docid : docid of the document to be scored
     * @param scoringFunction the scoring function to be applied
     * @return score of the document
     */
    public static double scoreDocument(int docid, ArrayList<PostingList> postingsToScore, String scoringFunction) {
        double score = 0.0;

        for (PostingList postingList : postingsToScore) {
            // Get the current posting
            Posting currPosting = postingList.getCurrentPosting();

            // If the current posting is not the one we are looking for, skip to the next one
            if (currPosting != null && currPosting.getDocid() == docid) {
                // Process the posting
                score += Scorer.scoreDocument(currPosting, Vocabulary.getInstance().getIdf(postingList.getTerm()), scoringFunction);

                postingList.next();
            }
        }
        return score;
    }

    /** method to process DAAT a list of posting list of the query terms using TFIDF as scoring function
     * @param queryPostings : list of postings of query terms
     * @param isConjuctive : if true, the query must be processed in CONJUNCTIVE way, else in DISJUNCTIVE way
     * @param k : number of top k documents to be returned
     * @param scoringFunction scoring function applied to calculate the score
     * @return returns a priority queue (of at most K elements) in the format <SCORE (Double), DOCID (Integer)> ordered by increasing score value
     */
    public static PriorityQueue<Map.Entry<Double, Integer>> scoreQuery(ArrayList<PostingList> queryPostings, boolean isConjuctive, int k, String scoringFunction){

        initialize(queryPostings);

        // initialization of the MinHeap for the results
        PriorityQueue<Map.Entry<Double, Integer>> topKDocuments = new PriorityQueue<>(k, Map.Entry.comparingByKey());

        int docToProcess = nextDocToProcess(isConjuctive, queryPostings);

        // until there are documents to be processed
        while(docToProcess!= -1){

            double docScore = scoreDocument(docToProcess, queryPostings, scoringFunction);

            // check if the MinHeap is full
            if(topKDocuments.size()==k){
                // MinHeap is full
                //System.out.println("heap is full\t");
                // check if the processed document can enter the MinHeap
                if (topKDocuments.peek() != null && docScore > topKDocuments.peek().getKey()) {
                    //System.out.println("current enters the heap\t");
                    // the current score enters the MinHeap

                    // remove the root of the MinHeap (the lowest score in top K documents)
                    topKDocuments.poll();

                    // insert the document and its score in the MinHeap
                    topKDocuments.add(new AbstractMap.SimpleEntry<>(docScore, docToProcess));
                }
            } else {
                // MinHeap is not full, the current document enters the MinHeap
                // System.out.println("heap is not full\t");
                // insert the document and its score in the MinHeap
                topKDocuments.add(new AbstractMap.SimpleEntry<>(docScore, docToProcess));
            }

            // find next document to be processed
            docToProcess = nextDocToProcess(isConjuctive, queryPostings);
        }
        // System.out.println("top K:\t"+topKDocuments);
        cleanUp(queryPostings);
        return topKDocuments;
    }
}
