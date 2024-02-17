package loader.src.main.java.loader;

import loader.TextDocument;
import loader.src.main.java.loader.Collection;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Loader {
//    private static final String DATA_PATH = "./data/collection.tsv";
    private static final String DATA_PATH = "./data/test_sample.tsv";
    public static void main(String[] args) {
        loadData().printCollection();
    }

    private static Collection loadData(){
        Collection collection = new Collection();
        int i = 0;

        try(BufferedReader br = Files.newBufferedReader(Paths.get(DATA_PATH), StandardCharsets.UTF_8)){
            String[] split;

            for(String line; (line = br.readLine()) != null;){
                // If it reaches end of file, read next line
                System.out.println(i);
                i++;
                if(line.isEmpty()) continue;

                split = line.split("\t");

                //Create the text document and insert into collection
                collection.addDocuments(new TextDocument(Integer.parseInt(split[0]), split[1]));
            }
        }
        catch (Exception e){
            System.out.println(i);
            e.printStackTrace();
        }
        return collection;
    }
}