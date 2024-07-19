package queryProcessing;

import it.unipi.dii.aide.mircv.beans.DocumentIndex;
import it.unipi.dii.aide.mircv.beans.Posting;
import it.unipi.dii.aide.mircv.config.CollectionSize;

/**
 * Scorer class
 */
public class Scorer {
    /**
     * parameter k1 for BM25
     */
    private static final double k1 = 1.5;

    /**
     * parameter b for BM25
     */
    private static final double b = 0.75;

    /**
     * score the posting using the specified scoring function
     * @param posting         the posting on which the scoring is performed
     * @param idf             the idf of the term related to the posting
     * @param scoringFunction the scoring function to use
     * @return the score for the posting
     */
    public static double scoreDocument(Posting posting, double idf, String scoringFunction) {
        return ((scoringFunction.equals("bm25")) ? computeBM25(posting, idf) : computeTFIDF(posting, idf));
    }

    /**
     * Compute the BM25 score for the posting
     * @param posting the posting on which the scoring is performed
     * @param idf     the idf of the term related to the posting
     * @return the BM25 score for the posting
     */
    private static double computeBM25(Posting posting, double idf) {
        double tf = (1 + Math.log10(posting.getFrequency()));

        // Get the document length and the average document length
        int docLength = DocumentIndex.getInstance().getLength(posting.getDocid());
        double avgDocLength = (double) CollectionSize.getTotalDocLen() / CollectionSize.getCollectionSize();

        return idf * tf / (tf + k1 * (1 - b + b * docLength / avgDocLength));
    }

    /**
     * Compute the TFIDF score for the posting
     * @param posting the posting on which the scoring is performed
     * @param idf     the idf of the term related to the posting
     * @return the TFIDF score for the posting
     */
    private static double computeTFIDF(Posting posting, double idf) {
        return idf * (1 + Math.log10(posting.getFrequency()));
    }
}
