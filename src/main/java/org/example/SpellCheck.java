package org.example;

import java.io.*;
import java.util.*;

class CuckooHashTable {
    private static final int SIZE = 200000; // Define the size of the hash tables
    private String[] table1 = new String[SIZE]; // First hash table
    private String[] table2 = new String[SIZE]; // Second hash table
    private static final int MAX_REHASHES = 20; // Increased rehash limit as a fallback

    private int hash1(String key) { // First hash function
        // Use a more robust hash function
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = 31 * hash + key.charAt(i);
        }
        return Math.abs(hash) % SIZE;
    }

    private int hash2(String key) { // Second hash function
        // Use a different multiplier and offset to reduce collisions
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = 17 * hash + key.charAt(i);
        }
        return (Math.abs(hash) + 1) % SIZE;
    }

    public boolean insert(String word) { // Insert a word into the hash tables
        int h1 = hash1(word);
        if (table1[h1] == null) { // If the slot in table1 is empty, insert the word
            table1[h1] = word;
            return true;
        }
        int h2 = hash2(word);
        if (table2[h2] == null) { // If the slot in table2 is empty, insert the word
            table2[h2] = word;
            return true;
        }
        return rehash(word, 0); // If both slots are occupied, attempt rehashing
    }

    private boolean rehash(String word, int depth) { // Handle rehashing when collisions occur
        if (depth >= MAX_REHASHES) { // Stop rehashing if the depth limit is reached
            System.err.println("Failed to insert word due to too many rehashes: " + word);
            return false;
        }
        int h1 = hash1(word);
        String displaced = table1[h1]; // Evict the current word at h1
        table1[h1] = word;
        int h2 = hash2(displaced);
        if (table2[h2] == null) { // If the displaced word can be placed in table2, do so
            table2[h2] = displaced;
            return true;
        }
        return rehash(displaced, depth + 1); // Otherwise, continue rehashing
    }

    public boolean contains(String word) { // Check if a word exists in the hash tables
        return (table1[hash1(word)] != null && table1[hash1(word)].equals(word)) ||
                (table2[hash2(word)] != null && table2[hash2(word)].equals(word));
    }
}

class Assignment2 {
    private CuckooHashTable dictionary = new CuckooHashTable(); // Create a dictionary using the Cuckoo Hash Table

    public void loadVocabulary(List<String> filePaths) throws IOException { // Load genres from multiple CSV files
        // Deduplicate genres before insertion
        Set<String> uniqueGenres = new HashSet<>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                int genreColumnIndex = -1;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        // Determine the index of the "Genre" column from the header
                        String[] headers = parseCsvLine(line);
                        for (int i = 0; i < headers.length; i++) {
                            if (headers[i].trim().equalsIgnoreCase("Genre")) {
                                genreColumnIndex = i;
                                break;
                            }
                        }
                        if (genreColumnIndex == -1) {
                            System.err.println("Genre column not found in file: " + filePath);
                            break;
                        }
                        isFirstLine = false;
                        continue;
                    }

                    // Parse the CSV line
                    String[] columns = parseCsvLine(line);
                    if (columns.length <= genreColumnIndex) {
                        continue; // Skip malformed rows
                    }

                    // Extract the "Genre" column
                    String genre = columns[genreColumnIndex].trim();
                    if (genre.isEmpty()) {
                        continue; // Skip empty genres
                    }

                    // Add to unique genres
                    uniqueGenres.add(genre.toLowerCase());
                }
            }
        }

        // Insert unique genres into the dictionary
        for (String genre : uniqueGenres) {
            dictionary.insert(genre);
        }
    }

    public boolean checkSpelling(String word) { // Check if a word exists in the dictionary
        return dictionary.contains(word.toLowerCase()); // Convert word to lowercase before checking
    }

    static List<String> loadWordsFromFiles(List<String> filePaths) throws IOException { // Helper method to load genres into a list
        List<String> words = new ArrayList<>(); // Initialize a list to store genres
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                int genreColumnIndex = -1;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        // Determine the index of the "Genre" column from the header
                        String[] headers = parseCsvLine(line);
                        for (int i = 0; i < headers.length; i++) {
                            if (headers[i].trim().equalsIgnoreCase("Genre")) {
                                genreColumnIndex = i;
                                break;
                            }
                        }
                        if (genreColumnIndex == -1) {
                            System.err.println("Genre column not found in file: " + filePath);
                            break;
                        }
                        isFirstLine = false;
                        continue;
                    }

                    // Parse the CSV line
                    String[] columns = parseCsvLine(line);
                    if (columns.length <= genreColumnIndex) {
                        continue; // Skip malformed rows
                    }

                    // Extract the "Genre" column
                    String genre = columns[genreColumnIndex].trim();
                    if (genre.isEmpty()) {
                        continue; // Skip empty genres
                    }

                    // Add the genre to the list in lowercase
                    words.add(genre.toLowerCase());
                }
            }
        }
        return words; // Return the list of genres
    }

    // Parses a CSV line, handling quoted fields
    private static String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // Toggle quote state
            } else if (c == ',' && !inQuotes) {
                // End of a field
                columns.add(field.toString().replace("\"", "")); // Remove quotes
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        // Add the last field
        columns.add(field.toString().replace("\"", ""));

        return columns.toArray(new String[0]);
    }

    public int levenshteinDistance(String a, String b) { // Compute Levenshtein distance between two words
        int[][] dp = new int[a.length() + 1][b.length() + 1]; // Create a DP table
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i; // Initialize the first column
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j; // Initialize the first row
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) { // If characters match, no cost
                    dp[i][j] = dp[i - 1][j - 1];
                } else { // Otherwise, take the minimum edit distance
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()]; // Return the computed distance
    }

    public String findNearestWord(String word, List<String> vocabulary) { // Find the closest matching word
        String nearest = null;
        int minDistance = Integer.MAX_VALUE; // Initialize with a high value
        for (String candidate : vocabulary) {
            int distance = levenshteinDistance(word, candidate); // Compute distance
            if (distance < minDistance) { // Keep track of the closest match
                minDistance = distance;
                nearest = candidate;
            }
        }
        return nearest; // Return the closest word
    }

    public static void main(String[] args) {
        Assignment2 spellChecker = new Assignment2(); // Create spell checker instance
        List<String> vocabularyFiles = Arrays.asList( // List of vocabulary file paths
                "data/britbox.csv",
                "data/CBCGem_Data.csv",
                "data/crave_data.csv",
                "data/prime_video_data.csv"
        );

        try {
            spellChecker.loadVocabulary(vocabularyFiles); // Load vocabulary from files
            List<String> vocabularyWords = loadWordsFromFiles(vocabularyFiles); // Load words into a list

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a word: ");
            String inputWord = scanner.next().toLowerCase(); // Read user input and convert to lowercase
            scanner.close();

            if (spellChecker.checkSpelling(inputWord)) { // Check if word is in dictionary
                System.out.println("The word is correct.");
            } else {
                String nearestWord = spellChecker.findNearestWord(inputWord, vocabularyWords); // Suggest nearest word
                System.out.println("Incorrect spelling. Nearest word: " + nearestWord);
            }
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            e.printStackTrace(); // Print error if file reading fails
        }
    }
}