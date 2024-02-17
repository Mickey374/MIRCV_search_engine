package loader.src.main.java.loader;

import loader.TextDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
public class Collection {
    private ArrayList<TextDocument> documents;

    // Initialize the arrayList of documents
    public Collection() {
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


    /**
     * Method to save collection to disk
     * @param path: Path to file
     **/
    public void writeToFile(String path){
//        System.out.println("Current Directory: " + System.getProperty("user.dir"));

        try{
            File outputFile = new File(path);
            if(outputFile.createNewFile()) {
                System.out.println("File created successfully: " + outputFile.getName());
            } else {
                System.out.println("File already exists.");
                try(PrintWriter writer = new PrintWriter(path)){
                    writer.print("");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            for(TextDocument doc: this.documents)
                Files.writeString(Paths.get(path), doc.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            System.out.println("An error occured while saving data to file.");
            e.printStackTrace();

        }
    }
}
