package it.unipi.dii.aide.mircv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO Object for the processed class
 */
public class ProcessedDocumentDTO {

    /**
     * Document Process Identifier (PID)
     */
    @JsonProperty("pid")
    private int pid;

    /**
     * Array with all processed terms
     */
    @JsonProperty("tokens")
    private String[] tokens;

    /**
     * Constructor of a document
     * @param pid PID of the doc
     * @param tokens array of processed tokens
     */
    public ProcessedDocumentDTO(int pid, String[] tokens){
        this.pid = pid;
        this.tokens = tokens;
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
    public int getPid() {
        return pid;
    }

    /**
     *
     * @param pid the PID to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     *
     * @return the array of the tokens
     */
    public String[] getTokens() {
        return tokens;
    }

    /**
     *
     * @param tokens The tokens set for the document
     */
    public void setTokens(String[] tokens) {
        this.tokens = tokens;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(pid + "\t");

        // If token length is empty/zero
        if(tokens.length == 0){
            str.append("\n");
            return str.toString();
        }

        // Append to the StringBuilder all tokens separated by ','
        for(int i=0; i < tokens.length -1; i++){
            str.append(tokens[i]);
            str.append(',');
        }

        // Last token should be without comma & also newline
        str.append(tokens[tokens.length -1]);
        str.append('\n');
        return str.toString();
    }
}