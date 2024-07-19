package it.unipi.dii.aide.mircv.preprocess;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.aide.mircv.beans.TextDocument;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.beans.ProcessedDocument;
import it.unipi.dii.aide.mircv.config.Flags;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that preprocesses the documents
 */
public class Preprocesser {
    /**
     * The pattern to match URLs
     */
    private static final String URL_MATCHER = "(?i)^(https?|ftp|mailto):\\/\\/(www\\.)?[a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,24}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$";

    /**
     * The pattern to match HTML tags
     */
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";

    /**
     * The pattern to match non-digits
     */
    private static final String NON_DIGIT_MATCHER = "[^a-zA-Z\\s]";

    /**
     * The pattern to match multiple spaces
     */
    private static final String MULTIPLE_SPACE_MATCHER = "\\s+";

    /**
     * regEx to match at least 3 consecutive letter
     */
    private static final String CONSECUTIVE_LETTERS_MATCHER = "(.)\\1{2,}";

    /**
     * regEx to match strings in Camel Case
     */
    private static final String CAMEL_CASE_MATCHER = "(?<=[a-z])(?=[A-Z])";

    /**
     * Path to file storing stopwords
     */
    protected static String PATH_TO_STOPWORDS = ConfigurationParams.getStopwordsPath();

    /**
     * List of Stopwords
     */
    private static final ArrayList<String> stopwords = new ArrayList<>();

    /**
     * Stemmer
     */
    private static final PorterStemmer stemmer = new PorterStemmer();

    /**
     * Match length for a term
     */
    private static final int TERM_THRESHOLD = 64;

    /**
     * Read stopwords from a file and loads them in main memory
     */
    public static void readStopwords() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_TO_STOPWORDS), StandardCharsets.UTF_8)) {
            for (String line; (line = br.readLine()) != null; ) {
                // If the line is empty, process the next line
                if (line.isEmpty()) continue;

                // Else add word to stopwords list
                stopwords.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tokenize the text
     * @param text: text to tokenize
     * @return list of tokens
     */
    public static String[] tokenizeWords(String text) {
        //list of tokens
        ArrayList<String> tokens = new ArrayList<>();

        // Split the text into tokens
        String[] splittedText = text.split("\\s");

        // For each token
        for (String token : splittedText) {
            // If the token is not empty, add it to the list
            String[] subtokens = token.split(CAMEL_CASE_MATCHER);
            for(String subtoken : subtokens){
                //if a token has a length over a certain threshold, cut it at the threshold value
                subtoken = subtoken.substring(0, Math.min(subtoken.length(), TERM_THRESHOLD));
                tokens.add(subtoken.toLowerCase(Locale.ROOT));
            }
        }
        return tokens.toArray(new String[0]);
    }


    /**
     * Performs text cleaning
     * @param text: text to clean
     * @return cleaned text
     */
    public static String cleanText(String text) {

        // Remove URLs
        text = text.replaceAll(URL_MATCHER, "\s");

        // Remove HTML tags
        text = text.replaceAll(HTML_TAGS_MATCHER, "\s");

        // Remove non-alphabetic characters
        text = text.replaceAll(NON_DIGIT_MATCHER, "\s");

        // Remove multiple spaces
        text = text.replaceAll(MULTIPLE_SPACE_MATCHER, "\s");

        // Remove consecutive letters
        text = text.replaceAll(CONSECUTIVE_LETTERS_MATCHER, "$1$1");

        // Remove extra spaces at the beginning and end of the text
        text = text.trim();

        return text;
    }

    /**
     * Remove stopwords from the text
     * @param tokens: list of tokens
     * @return list of tokens without stopwords
     */
    private static String[] removeStopwords(String[] tokens) {
        // ArrayList to hold valid tokens
        ArrayList<String> validTokens = new ArrayList<>();

        // Add the token to valid tokens if not a stop word
        for (String token : tokens)
            // If the token is not a stopword, add it to the list
            if (!stopwords.contains(token) && token.length() < TERM_THRESHOLD)
                validTokens.add(token);

        return validTokens.toArray(new String[0]);
    }

    /**
     * @param tokens: tokens to stem
     * @return Returns stem of each token
     */
    private static String[] getStemwords(String[] tokens) {
        // Replace each word with the stem word
        for (int i = 0; i < tokens.length; i++)
            tokens[i] = stemmer.stemWord(tokens[i]);

        return tokens;
    }

    /**
     * Perform the preprocessing of a TextDocument, transforming it in a document formed by
     * its PID and the list of its tokens
     * @param doc the TextDocument to preprocess
     * @return the processed document
     */
    public static ProcessedDocument preprocessDocument(TextDocument doc) {
        // Clean the text
        String cleanedText = cleanText(doc.getText());

        // Tokenize the text
        String[] tokens = tokenizeWords(cleanedText);

        // Remove stopwords
        tokens = removeStopwords(tokens);

        // Get the stems of the tokens
        tokens = getStemwords(tokens);

        // Check if there is a flag for stopword removal
        if(Flags.isStemStopRemovalEnabled()){
            // Remove stopwords
            tokens = removeStopwords(tokens);

            // Perform stemming
            getStemwords(tokens);
        }

        // Return the processed document
        return new ProcessedDocument(doc.getPid(), tokens);
    }

    /**
     * Used in test environment
     */
    protected static void setTestPath() {
        PATH_TO_STOPWORDS = "../config/stopwords.txt";
    }
}
