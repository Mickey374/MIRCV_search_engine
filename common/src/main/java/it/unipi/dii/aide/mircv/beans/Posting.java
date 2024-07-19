package it.unipi.dii.aide.mircv.beans;

public class Posting {
    /**
     * Document identifier
     */
    private int docid;

    /**
     * Term frequency in the document
     */
    private int frequency;

    /**
     * Default constructor with 0 params
     */
    public Posting() {}

    /**
     * Constructor for the posting of a specific document
     * and takes all information about the posting
     * @param docid the document id
     * @param frequency the frequency of the term in the document
     */
    public Posting(int docid, int frequency) {
        this.docid = docid;
        this.frequency = frequency;
    }

    /**
     * Get the document id
     * @return the document id
     */
    public int getDocid() {
        return docid;
    }

    /**
     * Set the document id
     * @param docid the document id
     */
    public void setDocid(int docid) {
        this.docid = docid;
    }

    /**
     * Get the frequency of the term in the document
     * @return the frequency of the term in the document
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Set the frequency of the term in the document
     * @param frequency the frequency of the term in the document
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Posting: {" +
                "docid=" + docid +
                ", frequency=" + frequency +
                '}';
    }
}
