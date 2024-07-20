package queryPerformances;

import it.unipi.dii.aide.mircv.beans.DocumentIndex;
import it.unipi.dii.aide.mircv.beans.PostingList;
import it.unipi.dii.aide.mircv.beans.ProcessedDocument;
import it.unipi.dii.aide.mircv.beans.TextDocument;
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


public class QueryPerformancesMain {
    private static final int k = 100;
    private static final String SCORING_FUNCTION = "bm25";
    private static final String QUERIES_PATH = "data/queries/queries.txt";
    private static final String TREC_EVAL_RESULTS_PATH = "data/queries/search_engine_results_" + SCORING_FUNCTION + ".txt";
    private static final boolean maxScore = false;
    private static final boolean isTrecEvalTest = false;
    private static final String fixed = "Q0";
    private static final String runid = "RUN-01";


    /**
     * TREC evaluation for results
     */
    private static boolean saveResultsforTrecEval(String topicId, PriorityQueue<Map.Entry<Double, Integer>> priorityQueue) {
        int i = priorityQueue.size();
        DocumentIndex documentIndex = DocumentIndex.getInstance();

        try(BufferedWriter statisticsBuffer = new BufferedWriter(new FileWriter(TREC_EVAL_RESULTS_PATH, true))) {
            String resultsLine;

            while (priorityQueue.peek() != null) {
                Map.Entry<Double, Integer> entry = priorityQueue.poll();
                resultsLine = topicId + "\t" + fixed + "\t" + documentIndex.getPid(entry.getValue()) + "\t" + i + "\t" + entry.getKey() + "\t" + runid + "\n";
                statisticsBuffer.write(resultsLine);
                i--;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Main method for the query processing
     */
    public static void main(String[] args) {
        System.out.println("Starting query processing...");
        boolean setupSuccess = QueryProcesser.setupProcesser();
        if (!setupSuccess) {
            System.out.println("Error in setting up the query processer");
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(QUERIES_PATH), StandardCharsets.UTF_8)) {
            // Read the queries from the file
            System.out.println("Processing queries...");

            String line;
            long sumResponseTime = 0;
            int numQueries = 0;
            ArrayList<Long> responseTimes = new ArrayList<>();

            while (true) {
                // If we reach end of file, break
                if ((line = br.readLine()) == null) {
                    System.out.println("End of file reached");
                    break;
                }
                // If line is empty, skip to the next line
                if (line.isBlank())
                    continue;

                // split of the line in the format <qid>\t<text>
                String[] split = line.split("\t");

                if (split.length != 2)
                    continue;

                // Create a new TextDocument object with the query
                TextDocument query = new TextDocument(split[0], split[1].replaceAll("[^\\x00-\\x7F]", ""));

                // Perform text processing on the document
                ProcessedDocument processedQuery = Preprocesser.preprocessDocument(query);

                // Perform the query processing
                ArrayList<PostingList> queryPostings = QueryProcesser.getQueryPostings(processedQuery, false);
                if (queryPostings == null || queryPostings.isEmpty())
                    continue;

                PriorityQueue<Map.Entry<Double, Integer>> priorityQueue;
                long start = System.currentTimeMillis();

                if (!maxScore) {
                    priorityQueue =DAAT.scoreQuery(queryPostings, false, k, SCORING_FUNCTION);
                } else {
                    priorityQueue = MaxScore.scoreQuery(queryPostings, k, SCORING_FUNCTION, false);
                }
                long end = System.currentTimeMillis();

                numQueries++;
                sumResponseTime += (end - start);
                responseTimes.add(end - start);

                if (isTrecEvalTest) {
                    if(!saveResultsforTrecEval(processedQuery.getPid(), priorityQueue)) {
                        System.out.println("Error in saving results for TREC evaluation");
                        return;
                    }
                }
            }

            double mean = sumResponseTime / (double) numQueries;
            double standardDeviation = 0.0;
            for(long time : responseTimes) {
                standardDeviation += Math.pow(time - mean, 2);
            }
            standardDeviation = Math.sqrt(standardDeviation / (double) numQueries);
            System.out.println("Query Response Time is: "+ (sumResponseTime / numQueries) + " ms, with a standard deviation of " + standardDeviation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}