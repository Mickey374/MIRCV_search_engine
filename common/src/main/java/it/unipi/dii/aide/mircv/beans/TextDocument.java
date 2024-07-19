package it.unipi.dii.aide.mircv.beans;

/**
 * The basic TextDocument, formed by an identifier (pid) and the text payload.
 */
public class TextDocument {
    /**
     * The document pid
     */
    private String pid;

    /**
     * The text of the document
     */
    private String text;

    /**
     * Default constructor with 0 params
     */
    public TextDocument() {}

    /**
     * Constructor for the text document
     * @param pid the pid of the document
     * @param text the text of the document
     */
    public TextDocument(String pid, String text) {
        this.pid = pid;
        this.text = text;
    }

    /**
     * Get the pid of the document
     * @return the pid of the document
     */
    public String getPid() {
        return pid;
    }

    /**
     * Set the pid of the document
     * @param pid the pid of the document
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * Get the text of the document
     * @return the text of the document
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text of the document
     * @param text the text of the document
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Formats the document as a '[pid] \t [text] \n' string
     * @return the formatted string
     */
    public String toString() {
        return pid + "\t" + text + "\n";
    }
}
