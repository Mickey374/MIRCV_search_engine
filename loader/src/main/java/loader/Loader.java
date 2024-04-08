package loader;


import beans.TextCollection;
import beans.TextDocument;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Loader {
//    private static final String DATA_PATH = "./data/collection.tsv";
    private static final String DATA_PATH = "./data/test_sample.tsv";
    public static void main(String[] args) {

//        loadData().printCollection();
        TextCollection c = loadData();
        c.writeToFile("./data/loadedData.tsv");
    }

    /**
     * The loadData method for the loader: This loads
     * the .tsv collection and parses the encoder to it
     * and saves the processed collection to disk.
     **/
    public static TextCollection loadData(){
        TextCollection collection = new TextCollection();

        try(BufferedReader br = Files.newBufferedReader(Paths.get(DATA_PATH), StandardCharsets.UTF_8)){
            String[] split;

            for(String line; (line = br.readLine()) != null;){
                // If it reaches end of file, read next line

                if(line.isEmpty()) continue;

                split = line.split("\t");

                System.out.println("Split -" + split[0]);

                //Create the text document and insert into collection
                collection.addDocuments(new TextDocument(Integer.parseInt(split[0]), split[1].replaceAll("[^\\x00-\\x7F]", "")));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return collection;
    }
}