package loader;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Loader {
    private static final String DATA_PATH = "./data/collection.tsv";
    public static void main(String[] args) {
        loadData().printCollection();
    }

    private static Collection loadData(){
        Collection collection = new Collection();

        try(BufferedReader br = Files.newBufferedReader(Paths.get(DATA_PATH), StandardCharsets.UTF_8)){
            for(String line; (line = br.readLine()) != null;){
                // If it reaches end of file, read next line
                if(line.isEmpty()) continue;

                String[] splitDocs = line.split("\t");

                //Create the text document and insert into collection
                TextDocument doc = new TextDocument(Integer.parseInt(splitDocs[0]), splitDocs[1]);
                collection.addDocuments(doc);
            }
        }

        catch (Exception e){
            e.printStackTrace();
        }

        return collection;
    }
}