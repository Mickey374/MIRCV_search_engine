package it.unipi.dii.aide.mircv.beans;

import java.util.ArrayList;
import java.util.List;

public class ProcessedDocument {
    /**
     * The document pid
     */
    private String pid;

    /**
     * Array with the processed terms
     */
    private ArrayList<String> tokens;

    /**
     * Default constructor with 0 params
     */
    public ProcessedDocument() {}

    /**
     * Constructor for the processed document
     * @param pid the pid of the document
     * @param tokens the list of tokens
     */
    public ProcessedDocument(String pid, String[] tokens) {
        this.pid = pid;
        this.tokens = new ArrayList<>(List.of(tokens));
    }

    /**
     * Get the pid of the document
     * @return the pid of the document
     */
    public String getPid() {
        return pid;
    }

    /**
     * Set the pid of the document
     * @param pid the pid of the document
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * Get the list of tokens
     * @return the list of tokens
     */
    public ArrayList<String> getTokens() {
        return tokens;
    }

    /**
     * Set the list of tokens
     * @param tokens the list of tokens
     */
    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    /**
     * Returns the processed document as a string formatted in the following way:
     * [pid] \t [token1,token2,token3,...,tokenN]
     * @return the formatted string
     */
    public String toString() {
        StringBuilder str = new StringBuilder(pid + "\t");

        if(tokens.isEmpty()) {
            str.append('\n');
            return str.toString();
        }

        //Append to the StringBuilder all the tokens than the last, separated by comma
        for(int i = 0; i < tokens.size() - 1; i++){
            str.append(tokens.get(i));
            str.append(',');
        }
        // Append the last element without the comma, then append the newline character
        str.append(tokens.get(tokens.size() - 1));
        str.append('\n');
        return str.toString();
    }
}
