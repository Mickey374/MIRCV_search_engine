package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.beans.TextDocument;
import it.unipi.dii.aide.mircv.preprocess.Preprocessor;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.dto.ProcessedDocumentDTO;
import it.unipi.dii.aide.mircv.utils.CollectionStats;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class MapDBLoader {
        /**
     * Path to the Raw collection
     */
    private static final String PATH_TO_COLLECTION = ConfigurationParams.getRawCollectionPath();

    /**
     * Path to the Document Collection processed
     */
    private static final String OUTPUT_PATH = ConfigurationParams.getProcessedCollectionPath();


    /**
     * Main Method for the Processing module
     * @param args input params for the main method
     * @throws Exception
     * <ol>
     *     <li>Load the stopwords into the Preprocessor</li>
     *     <li>Read the collection line by line</li>
     *     <li>Split the line in the format <pid>\t<text></li>
     *     <li>Creation of the text document for the line</li>
     *     <li>Perform the text preprocessing on the document</li>
     *     <li>Save it to the File if body is non-empty</li>
     *     <li>Update the number of Documents</li>
     *     <li>Close the DB</li>
     * </ol>
     */
    public static void main(String[] args) throws IOException {
        // Load the stopwords into the Preprocessor
        Preprocessor.readStopwords();

        // Create a DB
        try(BufferedReader br = Files.newBufferedReader(Paths.get(PATH_TO_COLLECTION), StandardCharsets.UTF_8);
            DB db = DBMaker.fileDB(OUTPUT_PATH).fileChannelEnable().fileMmapEnable().make();
            HTreeMap<String, ArrayList<String>> processedCollection = db.hashMap("processedCollection")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.JAVA)
                    .createOrOpen();) {
            String[] split;

            for (String line; (line = br.readLine()) != null; ) {

                // If line is empty then process the next line
                if (line.isEmpty()) continue;

                // Split the line in the format <pid>\t<text>
                split = line.split("\t");

                // Creation of the text document for the line
                TextDocument document = new TextDocument(split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));

                // Perform the text preprocessing on the document
                ProcessedDocumentDTO processedDocument = Preprocessor.processDocument(document);

                if (processedDocument.getTokens().size() > 0) {
                    // Save it to the File if body is non-empty
                    processedCollection.put(processedDocument.getPid(), processedDocument.getTokens());

                    // Update the number of Documents
                    CollectionStats.addDocument();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
            }
        }
    }