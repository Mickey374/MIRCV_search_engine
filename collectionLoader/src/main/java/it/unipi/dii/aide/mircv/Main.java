package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.beans.TextDocument;
import it.unipi.dii.aide.mircv.preprocess.Preprocessor;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.dto.ProcessedDocumentDTO;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static it.unipi.dii.aide.mircv.utils.FileUtils.CreateOrCleanFile;

/**
 * Main Class for the Collection Loader module
 * Performs UTF-8 and text processing of the collection,
 * and save the result on a file accessible by other modules
 */
public class Main {

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
     * @param args
     */
    public static void main(String[] args) {
        // Load the stopwords into the Preprocessor
        Preprocessor.readStopwords();

        // Clean or Create the Output file
        CreateOrCleanFile(OUTPUT_PATH);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_TO_COLLECTION), StandardCharsets.UTF_8)){
            String[] split;
            for(String line; (line = br.readLine()) != null;){

                // If line is empty then process the next line
                if(line.isEmpty()) continue;

                // Split the line in the format <pid>\t<text>
                split = line.split("\t");

                // Creation of the text document for the line
                TextDocument document = new TextDocument(Integer.parseInt(split[0]), split[1].replaceAll("[^\\x00-\\x7F]", ""));

                // Perform the text preprocessing on the document
                ProcessedDocumentDTO processedDocument = Preprocessor.processDocument(document);

                // Save it to the File
                Files.writeString(Paths.get(OUTPUT_PATH), processedDocument.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}