package cacheTests;

import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.ProcessedDocument;
import it.unipi.dii.aide.mircv.beans.TextDocument;
import it.unipi.dii.aide.mircv.beans.Vocabulary;
import it.unipi.dii.aide.mircv.preprocess.Preprocesser;

import queryProcessing.DAAT;
import queryProcessing.MaxScore;
import queryProcessing.QueryProcesser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

public class CacheTests {
    private static Vocabulary vocabulary = Vocabulary.getInstance();
    private static boolean maxScore = true;
    private static int k = 100;
    private static final String QUERIES_PATH = "data/queries/queries.txt";
    private static final String RESULTS_PATH = "data/queries/results.txt";
    private static final String STATS_PATH = "data/queries/stats.txt";
    private static String SCORING_FUNCTION = "tfidf";

    private static long timeNoCache = 0;
    private static long timeCache = 0;
    private static long totQueries = 0;

    /**
     * Method to process the queries and write the results in a file
     */
    private static void processQueries() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(QUERIES_PATH), StandardCharsets.UTF_8);
             BufferedWriter resultBuffer = new BufferedWriter(new FileWriter(RESULTS_PATH, true));
             ){
            // Read the queries from the file
            System.out.println("Processing queries...");

            String line;
            while ((line = br.readLine()) != null) {
                // If line is empty, skip to the next line
                if (line.isBlank())
                    continue;

                // split of the line in the format <qid>\t<text>
                String[] split = line.split("\t");

                if(split.length != 2)
                    continue;

                // Create a new TextDocument object with the query
                TextDocument query = new TextDocument(split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));

                // Perform text processing on the document
                ProcessedDocument processedQuery = Preprocesser.preprocessDocument(query);

                // Perform the query processing
                PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;

                long start = System.currentTimeMillis();
                ArrayList<PostingList> queryPostings = QueryProcesser.getQueryPostings(processedQuery, false);
                if(queryPostings == null || queryPostings.isEmpty())
                    continue;

                if(!maxScore)
                    priorityQueue = DAAT.scoreQuery(queryPostings, false, k, SCORING_FUNCTION);
                else
                    priorityQueue = MaxScore.scoreQuery(queryPostings, k, SCORING_FUNCTION, false);

                long end = System.currentTimeMillis();

                if(priorityQueue.isEmpty())
                    continue;

                long responseTime = end - start;
                System.out.println("Query " + processedQuery.getPid() + " processed in " + responseTime + " ms");
                resultBuffer.write(processedQuery.getPid() + "\t" + responseTime + "\t" + " no cache.\n");
                timeNoCache += responseTime;

                // Perform the query processing with cache
                start = System.currentTimeMillis();
                queryPostings = QueryProcesser.getQueryPostings(processedQuery, false);
                if(queryPostings == null || queryPostings.isEmpty())
                    continue;

                if(!maxScore)
                    DAAT.scoreQuery(queryPostings, false, k, SCORING_FUNCTION);
                else
                    MaxScore.scoreQuery(queryPostings, k, SCORING_FUNCTION, false);

                end = System.currentTimeMillis();

                responseTime = end - start;

                System.out.println("Query " + processedQuery.getPid() + " processed in " + responseTime + " ms");
                resultBuffer.write("\n" + processedQuery.getPid() + "\t" + responseTime + "\t" + " cache.\n");

                timeCache += responseTime;
                Vocabulary.clearCache();

                totQueries++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to run the tests
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting up...");
        boolean setupSuccess = QueryProcesser.setupProcesser();

        if(!setupSuccess) {
            System.out.println("Error in setting up the query processer");
            return;
        }

        System.out.println("Setup completed and Starting tests...");
        processQueries();

        SCORING_FUNCTION = "bm25";
        processQueries();

        try (BufferedWriter statsBuffer = new BufferedWriter(new FileWriter(STATS_PATH, true))) {
            double avgTimeNoCache = (double) timeNoCache / (double) totQueries;
            double avgTimeCache = (double) timeCache / (double) totQueries;

            statsBuffer.write("Average response time without cache: " + avgTimeNoCache + " ms\n");
            statsBuffer.write("Average response time with cache: " + avgTimeCache + " ms\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
