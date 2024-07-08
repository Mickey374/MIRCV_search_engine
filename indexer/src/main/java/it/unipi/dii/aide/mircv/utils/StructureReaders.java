package it.unipi.dii.aide.mircv.utils;

import it.unipi.dii.aide.mircv.beans.DocumentIndexEntry;
import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.VocabularyEntry;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StructureReaders {

    public static void readVocabulary(){
        // Use DB Maker to create a DB object of HashMap stored on disk
        DB db = DBMaker.fileDB(ConfigurationParams.getVocabularyPath()).make();

        // Create a map of vocabulary entries
        Map<String, VocabularyEntry> vocabulary = (Map<String, VocabularyEntry>) db.hashMap("vocabulary")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA).createOrOpen();

        // Print the vocabulary entries
        System.out.println("Vocabulary size: " +  vocabulary.size());

        // Read from Map
        for(String term: vocabulary.keySet()){
            System.out.println("Term: " + term + " - " + vocabulary.get(term));
        }

        // Close the database
        db.close();
    }

    public static void readDocIndex(){
        // Use DB Maker to create a DB object of HashMap stored on disk
        DB db = DBMaker.fileDB(ConfigurationParams.getDocumentIndexPath()).make();

        // Create a map of document index entries
        List<DocumentIndexEntry> docIndex = (List<DocumentIndexEntry>) db.indexTreeList("docIndex", Serializer.JAVA).createOrOpen();

        // Print the document index entries
        System.out.println("Document index size: " + docIndex.size());

        // Read from Map
        Iterator<DocumentIndexEntry> keys = docIndex.stream().iterator();
        while (keys.hasNext()) {
            System.out.println(keys.next());
        }

        // Close the database
        db.close();
    }

    public static void readInvertedIndex() {
        // Use DB Maker to create a DB object of HashMap stored on disk
        DB db = DBMaker.fileDB(ConfigurationParams.getVocabularyPath()).make();

        // Create a map of inverted index entries
        Map<String, VocabularyEntry> vocabularyDB = (Map<String, VocabularyEntry>) db.hashMap("vocabulary")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        Iterator<String> vocabulary = vocabularyDB.keySet().iterator();
        try(FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(ConfigurationParams.getInvertedIndexPath()), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)){

            while(vocabulary.hasNext()){
                VocabularyEntry term = vocabularyDB.get(vocabulary.next());
                long memOffset = term.getMemoryOffset();
                long freqOffset = term.getFrequencyOffset();
                long memSize = term.getMemorySize();
                long docSize = freqOffset - memOffset;

                // Instantiate the MappedByteBuffer for integer list of docIds
                MappedByteBuffer docBuffer = fc.map(FileChannel.MapMode.READ_WRITE, memOffset, docSize);

                // Instantiate the MappedByteBuffer for integer list of frequencies
                MappedByteBuffer freqBuffer = fc.map(FileChannel.MapMode.READ_WRITE, freqOffset, memSize - docSize);

                // Create the Posting List for the term
                PostingList postingList = new PostingList(term.getTerm());
                for(int i=0; i < term.getDf(); i++){
                    Map.Entry<Integer, Integer> posting = new AbstractMap.SimpleEntry<>(docBuffer.getInt(), freqBuffer.getInt());
                    postingList.getPostings().add(posting);
                }
                System.out.println(postingList);

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("+++++ DOCUMENT INDEX +++++");
        readDocIndex();
        Thread.sleep(2000);

        System.out.println("+++++ VOCABULARY +++++");
        readVocabulary();
        Thread.sleep(2000);

        System.out.println("+++++ INVERTED INDEX +++++");
        readInvertedIndex();
    }
}
