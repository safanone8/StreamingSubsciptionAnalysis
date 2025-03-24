package org.example;

import java.io.*;
import java.util.*;

public class pageRanking {
    // Class to represent a webpage with its ID and rank
    static class WebPage implements Comparable<WebPage> {
        String id;
        double rank;

        WebPage(String id, double rank) {
            this.id = id;
            this.rank = rank;
        }

        @Override
        public int compareTo(WebPage other) {
            return Double.compare(other.rank, this.rank); // Max-heap: higher rank first
        }
    }

    // Process a single webpage's content and return keyword frequencies
    private static Map<String, Integer> parseContent(String content) {
        Map<String, Integer> keywordFreq = new TreeMap<>(); // Red-Black Tree implementation
        String[] words = content.toLowerCase().split("\\W+"); // Split by non-word characters

        for (String word : words) {
            if (!word.isEmpty()) {
                keywordFreq.put(word, keywordFreq.getOrDefault(word, 0) + 1);
            }
        }
        return keywordFreq;
    }

    // Sort keywords by frequency using Heap Sort
    private static List<Map.Entry<String, Integer>> sortKeywordsByFreq(Map<String, Integer> keywordFreq) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(keywordFreq.entrySet());

        // Convert to array for heap sort
        Map.Entry<String, Integer>[] arr = list.toArray(new Map.Entry[0]);
        int n = arr.length;

        // Build max heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // Extract elements from heap one by one
        for (int i = n - 1; i > 0; i--) {
            Map.Entry<String, Integer> temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }

        return Arrays.asList(arr);
    }

    private static void heapify(Map.Entry<String, Integer>[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && arr[left].getValue() > arr[largest].getValue()) {
            largest = left;
        }
        if (right < n && arr[right].getValue() > arr[largest].getValue()) {
            largest = right;
        }

        if (largest != i) {
            Map.Entry<String, Integer> swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            heapify(arr, n, largest);
        }
    }

    // Calculate page rank based on search keywords
    private static double calculatePageRank(Map<String, Integer> keywordFreq, String[] searchKeywords) {
        double rank = 0.0;
        for (String keyword : searchKeywords) {
            rank += keywordFreq.getOrDefault(keyword.toLowerCase(), Integer.valueOf(0));
        }
        return rank;
    }

    public static void main(String[] args) {
        // Print the current working directory
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        // Assuming 4 files, each with 2 webpages separated by a delimiter (e.g., "---")
        String[] files = { "data/britbox.csv",
                "data/CBCGem_Data.csv",
                "data/crave_data.csv",
                "data/prime_video_data.csv"};
        String[] searchKeywords = {"plan", "tv", "premium"}; // Example search keywords
        PriorityQueue<WebPage> maxHeap = new PriorityQueue<>(); // Max-heap for top pages

        try {
            for (int i = 0; i < files.length; i++) {
                BufferedReader reader = new BufferedReader(new FileReader(files[i]));
                StringBuilder content = new StringBuilder();
                String line;
                int pageCount = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("---")) { // Delimiter for new webpage
                        if (content.length() > 0) {
                            processPage(content.toString(), "Page" + (i * 2 + pageCount),
                                    searchKeywords, maxHeap);
                            content.setLength(0); // Reset content
                            pageCount++;
                        }
                    } else {
                        content.append(line).append(" ");
                    }
                }
                // Process the last page in the file
                if (content.length() > 0) {
                    processPage(content.toString(), "Page" + (i * 2 + pageCount),
                            searchKeywords, maxHeap);
                }
                reader.close();
            }

            // Display top-ranked pages
            System.out.println("Top-ranked pages:");
            while (!maxHeap.isEmpty()) {
                WebPage page = maxHeap.poll();
                System.out.printf("Page ID: %s, Rank: %.2f%n", page.id, page.rank);
            }

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }
    }

    private static void processPage(String content, String pageId, String[] searchKeywords,
                                    PriorityQueue<WebPage> maxHeap) {
        // Parse content and get keyword frequencies
        Map<String, Integer> keywordFreq = parseContent(content);

        // Sort keywords by frequency
        List<Map.Entry<String, Integer>> sortedKeywords = sortKeywordsByFreq(keywordFreq);
        System.out.println("Sorted keywords for " + pageId + ": " + sortedKeywords);

        // Calculate rank
        double rank = calculatePageRank(keywordFreq, searchKeywords);

        // Add to max-heap
        maxHeap.offer(new WebPage(pageId, rank));
    }
}