package it.unipi.dii.aide.mircv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 * Class to map the processed Collection to JSON file
 * */
public class ProcessedCollectionDTO {
    // List of the processed documents in the collection
    @JsonProperty("data")
    private ArrayList<ProcessedDocumentDTO> data = new ArrayList<>();

    /**
     * Creates the object with the given list of processed docs to map into JSON
     * @param data The list of documents
     * */
    public ProcessedCollectionDTO(ArrayList<ProcessedDocumentDTO> data) {
        this.data = data;
    }

    /**
     *
     * 0-parameter constructor, instantiating the collection with no documents in the list
     */
    public ProcessedCollectionDTO(){
    }

    /**
    * @return The list of documents
    */
    public ArrayList<ProcessedDocumentDTO> getData() {
        return data;
    }

    /**
     * @param data the List of documents to set as the collection
     */
    public void setData(ArrayList<ProcessedDocumentDTO> data) {
        this.data = data;
    }
}
