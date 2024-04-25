package it.unipi.dii.aide.mircv.preprocess;

import it.unipi.dii.aide.mircv.beans.TextDocument;
import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.aide.mircv.config.ConfigurationParams;
import it.unipi.dii.aide.mircv.dto.ProcessedDocumentDTO;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
    private static final String URL_MATCHER = "(?i)^(https?|ftp|mailto):\\/\\/(www\\.)?[a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,24}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$";
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";
    private static final String NON_DIGIT_MATCHER = "[^a-zA-Z\s]";
    private static final String MULTIPLE_SPACE_MATCHER = "\s+";
    private static final String STOPWORDS_FILE = ConfigurationParams.getStopwordsPath();
    private static final String PATH_TO_OUTPUT_FILE = ConfigurationParams.getProcessedCollectionPath();

    private static final ArrayList<String> stopwords = new ArrayList<>();
    private static final PorterStemmer stemmer = new PorterStemmer();

    private static final Pattern URL_PATTERN = Pattern.compile(URL_MATCHER, Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAGS_PATTERN = Pattern.compile(HTML_TAGS_MATCHER);
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile(NON_DIGIT_MATCHER);
    private static final Pattern MULTIPLE_SPACE_PATTERN = Pattern.compile(MULTIPLE_SPACE_MATCHER);

    public static void readStopwords(){
        try(BufferedReader br = Files.newBufferedReader(Paths.get(STOPWORDS_FILE), StandardCharsets.UTF_8)){
            for(String line; (line = br.readLine()) != null;){
                // If the line is empty, process the next line
                if(line.isEmpty()) continue;

                // Else add word to stopwords list
                stopwords.add(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @param text:text to tokenize
     * Method to perform tokenization and return list of tokens
     **/
    public static String[] tokenizeWords(String text){
        return  text.split("\s");
    }

    /**
     * @param text:text to tokenize
     * Method to return text in lowerCase
     **/
    public static String lowerText(String text){
        return  text.toLowerCase(Locale.ROOT);
    }

    /**
     * @param text:text to tokenize
     * Method to return Processed text
     **/
    public static String cleanText(String text){
        // Remove the extra spaces at the beginning and end of the text
        text = text.trim();

        // Remove URLs
        text = replacePattern(text, URL_PATTERN, " ");

        // Remove HTML tags
        text = replacePattern(text, HTML_TAGS_PATTERN, " ");

        // Remove non-alphabetic characters
        text = replacePattern(text, NON_DIGIT_PATTERN, "");

        // Replace consecutive multiple whitespaces with a single one
        text = replacePattern(text, MULTIPLE_SPACE_PATTERN, " ");

        return text;
    }

    /**
     * @param text:text to replace
     * @param pattern:pattern to match text with
     * @param replacement:replacement pattern
     */
    private static String replacePattern(String text, Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll(replacement);
    }

    /**
     * @param tokens:The list of tokens
     * Method to remove stop words from list of tokens
     **/
    private static String[] removeStopwords(String[] tokens){
        // ArrayList to hold valid tokens
        ArrayList<String> validTokens = new ArrayList<>();

        // Add the token to valid tokens if not a stop word
        for(String token: tokens)
            if(!stopwords.contains(token))
                validTokens.add(token);

        return validTokens.toArray(new String[0]);
    }

    /**
     * @param tokens:The list of tokens
     * Method to return stems of each valid tokens
     **/
    private static String[] getStemwords(String[] tokens){
        // Replace each word with the stem word
        for(int i=0; i < tokens.length; i++)
            tokens[i] = stemmer.stemWord(tokens[i]);

        return tokens;
    }

    /**
     * Process the TextDocument in the pattern
     * pid [token[0], token[1], ...]
     * @param doc TextDocument to process
     * @return The processed document
     */
    public static ProcessedDocumentDTO processDocument(TextDocument doc) {
        String text = doc.getText();

        // Lower the Cases
        text = lowerText(text);

        // Clean the Text
        text = cleanText(text);

        // Tokenize
        String[] tokens = tokenizeWords(text);

        // Remove the Stopwords
        tokens = removeStopwords(tokens);

        // Stem tokens with PorterStemmer
        getStemwords(tokens);

        // Return the Processed Document
        return new ProcessedDocumentDTO(doc.getPid(), tokens);
    }

//    public static void main(String[] args){
//        readStopwords();
//
//        TextCollection collection = Loader.loadData();
//
//        try(FileWriter file = new FileWriter(PATH_TO_OUTPUT_FILE, true)){
//            for(TextDocument doc: collection.getDocuments()){
//                String text = doc.getText();
//
//                // lower text
//                text = lowerText(text);
//
//                // clean text
//                text = cleanText(text);
//
//                // tokenize text data
//                String[] tokens = tokenizeWords(text);
//
//                // remove stopWords
//                tokens = removeStopwords(tokens);
//
//                // performing stemming
//                tokens = getStemwords(tokens);
//
//                // write tokens into JSON file
//                JSONObject jsonObject = new JSONObject();
//
//                // Use the key-value pairs to insert into the object
//                jsonObject.put("doc_pid", doc.getDocId());
//
//                JSONArray terms = new JSONArray();
//
//                for(int i=0; i < tokens.length; i++)
//                    terms.add(tokens[i]);
//
//                Collections.addAll(terms, tokens);
//
//                jsonObject.put("terms", terms);
//
//                // write the object to a file
//                file.write(jsonObject.toJSONString() + "\n");
//            }
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
}
