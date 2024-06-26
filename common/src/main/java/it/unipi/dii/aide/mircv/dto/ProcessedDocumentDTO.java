package it.unipi.dii.aide.mircv.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO Object for the processed class
 */
public class ProcessedDocumentDTO {

    /**
     * Document Process Identifier (PID)
     */
    private String pid;

    /**
     * Array with all processed terms
     */
    private ArrayList<String> tokens;

    /**
     * Constructor of a document
     * @param pid PID of the doc
     * @param tokens array of processed tokens
     */
    public ProcessedDocumentDTO(String pid, String[] tokens){
        this.pid = pid;
        this.tokens = new ArrayList<>(List.of(tokens));
    }

    /**
     * 0-parameter constructor: instantiates the document
     */
    public ProcessedDocumentDTO() {
    }

    /**
     *
     * @return the document PID
     */
    public String getPid() {
        return pid;
    }

    /**
     *
     * @param pid the PID to set
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     *
     * @return the array of the tokens
     */
    public ArrayList<String> getTokens() {
        return tokens;
    }

    /**
     *
     * @param tokens The tokens set for the document
     */
    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    /*
        * Returns the processed document as a string formatted in this way:
        * @return the string representation of the document
        * PID [token[0], token[1], ...]
     */
    public String toString() {
        StringBuilder str = new StringBuilder(pid + "\t");

        // If token length is empty/zero
        if(tokens.isEmpty()){
            str.append("\n");
            return str.toString();
        }

        // Append to the StringBuilder all tokens separated by ','
        for(int i=0; i < tokens.size() -1; i++){
            str.append(tokens.get(i));
            str.append(',');
        }

        // Last token should be without comma & also newline
        str.append(tokens.get(tokens.size() -1));
        str.append('\n');
        return str.toString();
    }
}