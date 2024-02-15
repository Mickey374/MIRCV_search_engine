package preprocess;


import ca.rmen.porterstemmer.PorterStemmer;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {

    private static final String URL_MATCHER = "(?i)^(https?|ftp|mailto):\\/\\/(www\\.)?[a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,24}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$";
    private static final String HTML_TAGS_MATCHER = "<[^>]+>";
    private static final String NON_DIGIT_MATCHER = "[^a-zA-Z]";
    private static final String MULTIPLE_SPACE_MATCHER = "\s+";
    private static final String STOPWORDS_FILE = "data/stopwords-en.txt";
    private static ArrayList<String> stopwords = new ArrayList<>();
    public static PorterStemmer stemmer = new PorterStemmer();

    private static final Pattern URL_PATTERN = Pattern.compile(URL_MATCHER, Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAGS_PATTERN = Pattern.compile(HTML_TAGS_MATCHER);
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile(NON_DIGIT_MATCHER);
    private static final Pattern MULTIPLE_SPACE_PATTERN = Pattern.compile(MULTIPLE_SPACE_MATCHER);

    private static void readStopwords(){
        try(BufferedReader br = Files.newBufferedReader(Paths.get(STOPWORDS_FILE), StandardCharsets.UTF_8)){
            for(String line; (line = br.readLine()) != null;){
                // If the line is empty, process the next line
                if(line.isEmpty()) continue;

                // Else add word to stopwords list
                System.out.println(line);
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
        // Remove URLs
        text = replacePattern(text, URL_PATTERN, " ");

        // Remove HTML tags
        text = replacePattern(text, HTML_TAGS_PATTERN, " ");

        // Remove non-alphabetic characters
        text = replacePattern(text, NON_DIGIT_PATTERN, " ");

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
     * Method to remove stopwords from list of tokens
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

    public static void main(String[] args){
        String text = "We are living in a material world and I am a material girl";

//        for(String token: tokenizeWords(text))
//           System.out.println(token);

//        String url = "http://www.benny.com <benny> my regex is better than yours </benny>";
//         System.out.println(cleanText(url));

//        String punct = "heello... it's me.... ";
//        System.out.println("\n" + cleanText(punct));

//        String distant = "torniamo          vicini!! ";
//         System.out.println("\n" + cleanText(distant));

//        readStopwords();

        String[] tokens = tokenizeWords("Above the bench the goat lives below the bench the goat dies");
        tokens = getStemwords(tokens);
        for(String token: tokens)
            System.out.println(token);
    }

}
