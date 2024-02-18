package loader.src.main.java.beans;

public class TextDocument {
    /**
     * id for the document
     **/
    private int docId;

    /**
     * text of the document
     **/
    private String text;

    /**
     * Creates a new TextDocument with the given identifier and payload
     * @param docId the document's identifier
     * @param text the document's payload
     */
    public TextDocument(int docId, String text){
        this.docId = docId;
        this.text = text;
    }

    /**
     * gets the identifier
     * @return the identifier of the document
     */
    public int getDocId(){
        return docId;
    }

    /**
     * sets the pid
     * @param docId the identifier of the document
     */
    public void setDocId(int docId){
        this.docId = docId;
    }

    /**
     * gets the document's text
     * @return the text payload of the document
     */
    public String getText(){
        return text;
    }

    /**
     * sets the document's text
     * @param text the document's payload
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Formats the document as a '[docid] \t [text] \n' string
     * @return the formatted string
     */
    public String toString(){
        return Integer.toString(docId) + "\t" + text + "\n";
    }
}
