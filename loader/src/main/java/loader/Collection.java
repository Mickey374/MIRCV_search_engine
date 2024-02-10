package loader;

import java.util.ArrayList;
public class Collection {
    private ArrayList<TextDocument> documents = new ArrayList<TextDocument>();

//     Method to add documents to arrayList
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
