package loader;

public class TextDocument {
    private int docId;
    private String text;

    public TextDocument(int docId, String text){
        this.docId = docId;
        this.text = text;
    }

    public int getDocId(){
        return docId;
    }

    public void setDocId(int docId){
        this.docId = docId;
    }

    public String getText(){
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString(){
        return Integer.toString(docId) + "\t" + text + "\n";
    }
}
