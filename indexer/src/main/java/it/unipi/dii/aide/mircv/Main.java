package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.algorithms.Merger;
import it.unipi.dii.aide.mircv.algorithms.Spimi;
import static it.unipi.dii.aide.mircv.utils.Utility.initializeFiles;

public class Main {
    public static void main(String[] args) {
        initializeFiles();
//        Spimi.executeSpimi();
//        Merger.mergeIndexes();
//        System.out.println("Indexing completed!");
        int numIndexes = Spimi.executeSpimi();
        if(numIndexes == 0){
            System.out.println("An error occured: no partial indexes");
            return;
        }
        if(Merger.mergeIndexes(numIndexes)) {
            System.out.println("Inverted index created successfully");
            return;
        }
        System.out.println("An error occured during merging");
    }
}