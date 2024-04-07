package beans;


import java.util.ArrayList;

public class TextCollection {
    private ArrayList<TextDocument> documents;

    // Initialize the arrayList of documents
    public TextCollection() {
         this.documents = new ArrayList<>();
    }

//     Method to add documents to arrayList collection
    public void addDocuments(TextDocument doc) {
            documents.add(doc);
    }

    // Function to return documents
    public ArrayList<TextDocument> getDocuments() {
        return documents;
    }

    // Function to set documents in ArrayList
    public void setDocuments(ArrayList<TextDocument> documents){
        this.documents = documents;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();

        for(TextDocument doc: this.documents){
            builder.append(doc.toString());
        }
        return builder.toString();
    }

    public void printCollection(){
        for(TextDocument doc: this.documents) {
            System.out.println(doc);
        }
    }
}
