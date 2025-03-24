package org.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// This is our TrieNode class - think of it as a building block for our word tree!
class TrieNode {
    Map<Character, TrieNode> children; // A map to store the next characters
    boolean isEndOfWord;               // A flag to mark if this node ends a word

    TrieNode() {
        children = new HashMap<>();    // Initialize an empty map for kids (child nodes)
        isEndOfWord = false;           // By default, this isn’t the end of a word
    }
}

public class WordCompletionTrie {
    private TrieNode vasu_root; // The starting point of our word tree
    private static final Pattern vasu_WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]"); // Regex to clean up non-alphanumeric junk

    // Constructor - just sets up a fresh Trie
    public WordCompletionTrie() {
        vasu_root = new TrieNode(); // Let’s plant the root of our word tree here!
    }

    // This method inserts a word into our Trie, one character at a time
    public void insert(String vasu_word) {
        TrieNode vasu_current = vasu_root; // Start at the root of the tree
        for (char vasu_c : vasu_word.toCharArray()) { // Loop through each character
            // If this character isn’t a child yet, let’s give it a home
            vasu_current.children.putIfAbsent(vasu_c, new TrieNode());
            // Move down to the next level of the tree
            vasu_current = vasu_current.children.get(vasu_c);
        }
        // We’ve added all characters, so mark this as a complete word
        vasu_current.isEndOfWord = true;
    }

    // A handy public method to add a word after cleaning it up
    public void addWord(String vasu_word) {
        // Strip out anything that’s not a letter or number and make it lowercase
        String vasu_cleanedWord = vasu_WORD_PATTERN.matcher(vasu_word).replaceAll("").toLowerCase();
        // Only bother inserting if we’ve got something left after cleaning
        if (!vasu_cleanedWord.isEmpty()) {
            insert(vasu_cleanedWord); // Off it goes into the Trie!
        }
    }

    // Helper method to dig through the Trie and find all words with a prefix
    private void findWordsWithPrefix(TrieNode vasu_node, String vasu_prefix, List<String> vasu_results, StringBuilder vasu_currentWord) {
        // If we’ve hit the end of a word, add it to our results list
        if (vasu_node.isEndOfWord) {
            vasu_results.add(vasu_currentWord.toString());
        }

        // Explore all child nodes (like branches of a tree)
        for (char vasu_c : vasu_node.children.keySet()) {
            vasu_currentWord.append(vasu_c); // Add the next letter to our word
            // Recursively keep searching down this path
            findWordsWithPrefix(vasu_node.children.get(vasu_c), vasu_prefix, vasu_results, vasu_currentWord);
            // Backtrack by removing the last letter so we can try other branches
            vasu_currentWord.setLength(vasu_currentWord.length() - 1);
        }
    }

    // The star of the show: get word suggestions based on a prefix!
    public List<String> getSuggestions(String vasu_prefix, int vasu_maxSuggestions) {
        List<String> vasu_results = new ArrayList<>(); // Where we’ll store our suggestions
        TrieNode vasu_current = vasu_root; // Start at the top of the tree
        vasu_prefix = vasu_prefix.toLowerCase(); // Keep everything lowercase for consistency

        // Let’s navigate to the node that matches our prefix
        for (char vasu_c : vasu_prefix.toCharArray()) {
            // If the character isn’t in the tree, no matches exist - bail out!
            if (!vasu_current.children.containsKey(vasu_c)) {
                return new ArrayList<>(); // Empty list, nothing to see here
            }
            vasu_current = vasu_current.children.get(vasu_c); // Move deeper into the tree
        }

        // Now that we’re at the prefix’s end, find all possible words
        findWordsWithPrefix(vasu_current, vasu_prefix, vasu_results, new StringBuilder(vasu_prefix));

        // If we’ve got too many suggestions, trim it down to the max allowed
        if (vasu_results.size() > vasu_maxSuggestions) {
            return vasu_results.subList(0, vasu_maxSuggestions);
        }
        return vasu_results; // Here’s your list of suggestions!
    }

    // Loads words from a CSV file - like slurping up a word soup!
    private void loadVocabularyFromCSV(String vasu_filePath) {
        try (BufferedReader vasu_br = new BufferedReader(new FileReader(vasu_filePath))) {
            String vasu_line;
            // Keep reading lines until we hit the end
            while ((vasu_line = vasu_br.readLine()) != null) {
                String[] vasu_values = vasu_line.split(","); // Split by commas
                for (String vasu_value : vasu_values) {
                    // Split again by spaces and add each word
                    Arrays.stream(vasu_value.trim().split("\\s+")).forEach(this::addWord);
                }
            }
            System.out.println("Loaded vocabulary from CSV: " + vasu_filePath);
        } catch (IOException vasu_e) {
            // Oops, something went wrong - let’s complain about it
            System.err.println("Error reading CSV file " + vasu_filePath + ": " + vasu_e.getMessage());
        }
    }


    // A convenience method to load all our vocab files at once
    public void loadAllVocabularies() {
        loadVocabularyFromCSV("data/britbox.csv");
        loadVocabularyFromCSV("data/CBCGem_data.csv");
        loadVocabularyFromCSV("data/crave_data.csv");
        loadVocabularyFromCSV("data/prime_video_data.csv");


    }

    // Let’s get this party started!
    public static void main(String[] vasu_args) {
        WordCompletionTrie vasu_wc = new WordCompletionTrie(); // Create our Trie instance
        vasu_wc.loadAllVocabularies(); // Load up all the words we’ve got

        // Set up a reader to grab input from the user
        try (BufferedReader vasu_consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("\nEnter a prefix (or 'exit' to quit):");
            String vasu_prefix;
            // Keep asking for prefixes until the user says "exit"
            while (!(vasu_prefix = vasu_consoleReader.readLine()).equalsIgnoreCase("exit")) {
                if (vasu_prefix != null && !vasu_prefix.trim().isEmpty()) { // Check for valid input
                    List<String> vasu_suggestions = vasu_wc.getSuggestions(vasu_prefix, 5); // Get up to 5 suggestions
                    System.out.println("Suggestions for '" + vasu_prefix + "': " + vasu_suggestions);
                } else {
                    System.out.println("Please enter a valid prefix."); // Nudge them to try again
                }
                System.out.println("\nEnter a prefix (or 'exit' to quit):"); // Prompt again
            }
            System.out.println("Exiting program."); // Time to say goodbye!
        } catch (IOException vasu_e) {
            System.err.println("Error reading console input: " + vasu_e.getMessage());
        }
    }
}