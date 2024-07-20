package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.config.Flags;
import queryProcessing.QueryProcesser;

import java.util.Locale;
import java.util.Scanner;


public class Main {
        /**
         * Define the top documents to return
         */
        private static final int k = 10;

    /**
     * Executes the startup of the application and exposed the user interface
     * @param args the scoring algorithm to be used, if none DAAT will be used as default
     */
    public static void main(String[] args) {
        System.out.println("Welcome to the MIR-CV search engine!");
        System.out.println("Starting...");

        // Check if setup of data structures was successful
        boolean setupSuccess = QueryProcesser.setupProcesser();
        if (!setupSuccess) {
            System.out.println("Error during the setup of the query processor. Exiting...");
            return;
        }

        // Set the locale to English
        Locale.setDefault(Locale.ENGLISH);

        // Initialize the scanner
        if (args.length > 0) {
            if(args[0].equals("-maxscore")) {
                Flags.setMaxScore(true);
            } else {
                System.out.println("Invalid argument. Exiting...");
            }
        }

        // Initialize the scanner
        Scanner scanner = new Scanner(System.in);

        String query;

        System.out.println("What are you looking for? " + """
                Please insert a query specifying your preferred mode:\s
                -c for conjunctive mode or -d for disjunctive mode. Here's an example:\s
                This is a query example -c \s
                Type "help" to get help or "break" to terminate the service""");

        for(;;) {
            System.out.println("What are you looking for? Type \"help\" to get help or \"break\" to terminate the service to terminate the service");
            query = scanner.nextLine();

            // Check if the user wants to break the loop
            if(query == null || query.isEmpty()) {
                System.out.println("The query you entered is empty.");
                continue;
            }

            String[] queryParts = query.split("-");

            if(queryParts.length == 1){
                // Check if the user wants to break the loop
                if(queryParts[0].equals("break")) {
                    System.out.println("Exiting application...");
                    break;
                }

                // Check if the user wants help
                if(queryParts[0].equals("help")) {
                    System.out.println("Please insert a query specifying your preferred mode:\n" +
                            "-c for conjunctive mode or -d for disjunctive mode. Here's an example:\n" +
                            "This is a query example -c");
                    continue;
                }

                // Check if user request is invalid
                System.out.println("Invalid query. Please insert a query specifying your preferred mode:\n" +
                        "-c for conjunctive mode or -d for disjunctive mode. Here's an example:\n" +
                        "This is a query example -c");
                continue;
            }

//            Check if query is valid
            if(!queryParts[1].equals("c") && !queryParts[1].equals("d")) {
                System.out.println("Invalid query. Please insert a query specifying your preferred mode:\n" +
                        "-c for conjunctive mode or -d for disjunctive mode. Here's an example:\n" +
                        "This is a query example -c");
                continue;
            }

            // Perform the query processing
            System.out.println("Which scoring function would you like to apply?\n Please insert \'tfidf\' or \'bm25\'");
            String scoringFunction = scanner.nextLine().toLowerCase(Locale.ROOT);

            while (!scoringFunction.equals("tfidf") && !scoringFunction.equals("bm25")) {
                System.out.println("Invalid scoring function. Please insert \'tfidf\' or \'bm25\'");
                scoringFunction = scanner.nextLine().toLowerCase(Locale.ROOT);
            }

            // Third parameter if true means query mode is conjuctive
            long start = System.currentTimeMillis();
            String[] topKDocuments = QueryProcesser.processQuery(queryParts[0], k, queryParts[1].equals("c"), scoringFunction);
            long end = System.currentTimeMillis();

            if (topKDocuments == null || topKDocuments[0] == null) {
                System.out.println("No documents found for the query.");
                continue;
            }

            System.out.println("Top " + k + " documents for the query \"" + queryParts[0] + "\" in" + (end - start) + "ms are:\n");
            for(String document: topKDocuments) {
                if(document != null)
                    System.out.println("--> " + document);
            }
        }
        // Close the scanner
        scanner.close();
    }
}