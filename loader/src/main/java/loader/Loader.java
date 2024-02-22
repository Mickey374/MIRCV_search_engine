package loader.src.main.java.loader;


import loader.src.main.java.beans.TextCollection;
import loader.src.main.java.beans.TextDocument;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Loader {
//    private static final String DATA_PATH = "./data/collection.tsv";
    private static final String DATA_PATH = "./data/test_sample.tsv";
    public static void main(String[] args) {

        loadData().printCollection();
//        TextCollection c = loadData();
//        c.writeToFile("./data/loadedData.tsv");
    }

    public static TextCollection loadData(){
        TextCollection collection = new TextCollection();

        try(BufferedReader br = Files.newBufferedReader(Paths.get(DATA_PATH), StandardCharsets.UTF_8)){
            String[] split;

            for(String line; (line = br.readLine()) != null;){
                // If it reaches end of file, read next line

                if(line.isEmpty()) continue;

                split = line.split("\t");

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