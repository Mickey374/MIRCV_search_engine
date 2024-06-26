package it.unipi.dii.aide.mircv.beans;

public class TextDocument {
    /**
     * id for the document
     **/
    private String pid;

    /**
     * text of the document
     **/
    private String text;

    /**
     * Creates a new TextDocument with the given identifier and payload
     * @param pid the document's identifier
     * @param text the document's payload
     */
    public TextDocument(String pid, String text){
        this.pid = pid;
        this.text = text;
    }

    /**
     * gets the identifier
     * @return the identifier of the document
     */
    public String getPid(){
        return pid;
    }

    /**
     * sets the pid
     * @param pid the identifier of the document
     */
    public void setPid(String pid){
        this.pid = pid;
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
        return pid + "\t" + text + "\n";
    }
}