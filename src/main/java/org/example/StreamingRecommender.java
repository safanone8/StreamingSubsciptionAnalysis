package org.example;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class StreamingRecommender {
    private Map<String, ServiceData> services;
    private List<String> allGenres;
    private WordCompletionTrie wordCompletionTrie;
    private Assignment2 spellChecker;
    private Map<String, Integer> genreFrequencies;

    // Helper method to determine quality level
    private int getQualityLevel(String quality) {
        String q = quality.toLowerCase().trim();
        switch (q) {
            case "hd": return 1;
            case "full hd": return 2;
            case "4k uhd": return 3;
            default: return 0; // Unknown quality
        }
    }
    // Helper class to store service data
    public static class ServiceData {
        String name;
        Map<String, Double> monthlyPlans = new HashMap<>();
        Map<String, Double> yearlyPlans = new HashMap<>();
        List<String> genres = new ArrayList<>();
        Map<String, String> planQualities = new HashMap<>();
        Map<String, Integer> planDevices = new HashMap<>();
        Map<String, Boolean> planDownloads = new HashMap<>();
        List<String> regions = new ArrayList<>();

        public ServiceData(String name) {
            this.name = name;
        }
    }
    // Load service data from CSV files
    public StreamingRecommender() {
        services = wordFrequencyCount.loadServiceData();
        genreFrequencies = wordFrequencyCount.getGenreFrequencies();
        wordCompletionTrie = new WordCompletionTrie();
        spellChecker = new Assignment2();
        List<String> vocabularyFiles = Arrays.asList(
                "data/paramount_plus_data.csv",
                "data/prime_video_data.csv",
                "data/britbox.csv",
                "data/CBCGem_Data.csv",
                "data/crave_data.csv"
        );
        try {
            wordCompletionTrie.loadAllVocabularies();
            spellChecker.loadVocabulary(vocabularyFiles); // Load vocabulary for spell checker
        } catch (IOException e) {
            System.err.println("Error loading vocabularies: " + e.getMessage()); // Print error message
        }
        Set<String> genreSet = new HashSet<>();
        for (ServiceData service : services.values()) {
            genreSet.addAll(service.genres);
        }
        allGenres = new ArrayList<>(genreSet);
    }

    // Select genre with word completion and spell check fallback
    public String selectGenreFromSuggestions(String input) {
        List<String> suggestions;
        final int MAX_SUGGESTIONS = 10;
        suggestions = wordCompletionTrie.getSuggestions(input, Integer.MAX_VALUE);
        if (suggestions.isEmpty()) {
            System.out.println("No matching genres found with word completion. Falling back to spell check...");
            try {
                Set<String> uniqueGenres = new HashSet<>(allGenres);
                List<Map.Entry<String, Integer>> distances = new ArrayList<>();
                for (String genre : uniqueGenres) {
                    int distance = spellChecker.levenshteinDistance(input, genre);
                    distances.add(new AbstractMap.SimpleEntry<>(genre, distance));
                }
                // Sort by distance, then by frequency
                distances.sort((a, b) -> {
                    int distCompare = Integer.compare(a.getValue(), b.getValue());
                    if (distCompare != 0) return distCompare;
                    int freqA = genreFrequencies.getOrDefault(a.getKey(), 0);
                    int freqB = genreFrequencies.getOrDefault(b.getKey(), 0);
                    return Integer.compare(freqB, freqA);
                });
                suggestions = distances.stream()
                        .map(Map.Entry::getKey)
                        .limit(MAX_SUGGESTIONS)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error during spell check: " + e.getMessage());
                return null;
            }
        }

        if (suggestions.isEmpty()) {
            System.out.println("No matching genres found.");
            return null;
        }
        // Display suggestions
        List<Map.Entry<String, Integer>> rankedSuggestions = new ArrayList<>();
        for (String suggestion : suggestions) {
            String normalized = normalizeGenre(suggestion.toLowerCase());
            int freq = genreFrequencies.getOrDefault(normalized, 0);
            rankedSuggestions.add(new AbstractMap.SimpleEntry<>(normalized, freq));
        }
        Map<String, Integer> uniqueRankedSuggestions = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : rankedSuggestions) {
            uniqueRankedSuggestions.putIfAbsent(entry.getKey(), entry.getValue());
        }
        rankedSuggestions = new ArrayList<>(uniqueRankedSuggestions.entrySet());
        // Sort by frequency, then by name
        rankedSuggestions.sort((a, b) -> {
            int freqCompare = Integer.compare(b.getValue(), a.getValue());
            if (freqCompare != 0) return freqCompare;
            return a.getKey().compareTo(b.getKey());
        });

        System.out.println("Possible genres:");
        for (int i = 0; i < rankedSuggestions.size(); i++) {
            System.out.println((i + 1) + ". " + rankedSuggestions.get(i).getKey());
        }
        // Select genre
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select a genre by number (or 0 to skip): ");
        int choice;
        try {
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            System.out.println("Invalid input. Skipping this genre.");
            scanner.nextLine(); // Clear buffer
            return null;
        }
        // Return selected genre
        if (choice == 0) {
            return null;
        } else if (choice < 1 || choice > rankedSuggestions.size()) {
            System.out.println("Invalid selection. Skipping this genre.");
            return null;
        }

        return rankedSuggestions.get(choice - 1).getKey();
    }

    // Select region with spell check
    public String selectRegionFromSuggestions(String input) {
        List<String> allRegions = new ArrayList<>();
        for (ServiceData service : services.values()) {
            allRegions.addAll(service.regions);
        }
        Set<String> uniqueRegions = new HashSet<>(allRegions);
        List<Map.Entry<String, Integer>> distances = new ArrayList<>();
        for (String region : uniqueRegions) {
            int distance = spellChecker.levenshteinDistance(input.toLowerCase(), region.toLowerCase());
            distances.add(new AbstractMap.SimpleEntry<>(region, distance));
        }
        distances.sort(Comparator.comparingInt(Map.Entry::getValue));
        // Get top 5 suggestions
        List<String> suggestions = distances.stream()
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());

        if (suggestions.isEmpty()) {
            System.out.println("No matching regions found.");
            return null;
        }
        // Display suggestions
        System.out.println("Possible regions (ranked by relevance):");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.println((i + 1) + ". " + suggestions.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Select a region by number (or 0 to skip): ");
        int choice;
        try {
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            System.out.println("Invalid input. Skipping this region.");
            scanner.nextLine(); // Clear buffer
            return null;
        }

        if (choice == 0) {
            return null;
        } else if (choice < 1 || choice > suggestions.size()) {
            System.out.println("Invalid selection. Skipping this region.");
            return null;
        }

        return suggestions.get(choice - 1);
    }

    // Count matching genres for a service
    public int countMatchingGenres(ServiceData service, List<String> preferredGenres) {
        int count = 0;
        for (String genre : preferredGenres) {
            String normalizedGenre = normalizeGenre(genre.toLowerCase());
            for (String serviceGenre : service.genres) {
                if (normalizeGenre(serviceGenre.toLowerCase()).equals(normalizedGenre)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    // Rank services using page ranking
    public List<String> rankServices(String planType, double maxBudget, String videoQuality, int devices,
                                     String region, boolean requireDownload, List<String> preferredGenres) {
        List<Map.Entry<String, Double>> scoredServices = new ArrayList<>();

        for (Map.Entry<String, ServiceData> entry : services.entrySet()) {
            String serviceName = entry.getKey();
            ServiceData service = entry.getValue();

            // Check region availability
            if (!service.regions.contains(region)) {
                continue;
            }

            // Check plan type
            Map<String, Double> plans = planType.equalsIgnoreCase("monthly") ? service.monthlyPlans : service.yearlyPlans;
            if (plans.isEmpty()) {
                continue;
            }

            // Find eligible plans
            for (Map.Entry<String, Double> plan : plans.entrySet()) {
                String planName = plan.getKey();
                double planCost = plan.getValue();

                // Check budget
                if (planCost > maxBudget) {
                    continue;
                }

                // Check video quality
                int requestedLevel = getQualityLevel(videoQuality);
                if (requestedLevel == 0) {
                    continue; // Skip if requested quality is unknown
                }
                String qualityStr = service.planQualities.getOrDefault(planName, "");
                if (qualityStr.isEmpty()) {
                    continue; // Skip if no quality is specified
                }
                String[] qualities = qualityStr.split(",\\s*"); // Split by comma and optional whitespace
                int serviceMaxLevel = 0;
                for (String q : qualities) {
                    int level = getQualityLevel(q);
                    if (level > serviceMaxLevel) {
                        serviceMaxLevel = level;
                    }
                }
                if (serviceMaxLevel < requestedLevel) {
                    continue; // Skip if service doesn’t meet the quality requirement
                }

                // Check devices
                int planDevices = service.planDevices.getOrDefault(planName, 0);
                if (planDevices < devices) {
                    continue;
                }

                // Check download
                boolean hasDownload = service.planDownloads.getOrDefault(planName, false);
                if (requireDownload && !hasDownload) {
                    continue;
                }

                // Calculate genre match score
                int genreMatches = countMatchingGenres(service, preferredGenres);
                if (genreMatches == 0) {
                    continue; // Exclude services with no genre matches
                }
                double genreScore = (double) genreMatches / preferredGenres.size();

                // Calculate cost score
                double costScore = (maxBudget - planCost) / maxBudget;

                // Total score: 60% genre match, 40% cost
                double totalScore = (genreScore * 0.6) + (costScore * 0.4);
                scoredServices.add(new AbstractMap.SimpleEntry<>(serviceName + " (" + planName + " $" + planCost + ")", totalScore));
            }
        }

        scoredServices.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<String> recommendations = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scoredServices) {
            recommendations.add(String.format("%s - Score: %.2f", entry.getKey(), entry.getValue()));
        }
        return recommendations;
    }

    // Check if the service quality meets or exceeds the requested quality
    public boolean isQualityCompatible(String serviceQuality, String requestedQuality) {
        if (serviceQuality.equalsIgnoreCase(requestedQuality)) {
            return true;
        }
        if (serviceQuality.equalsIgnoreCase("4K UHD")) {
            return requestedQuality.equalsIgnoreCase("Full HD") || requestedQuality.equalsIgnoreCase("HD");
        }
        if (serviceQuality.equalsIgnoreCase("Full HD")) {
            return requestedQuality.equalsIgnoreCase("HD");
        }
        return false;
    }

    // New method to get recommendations
    public List<String> getRecommendations(String planType, double maxBudget, String videoQuality,
                                           int devices, String region, boolean requireDownload,
                                           List<String> preferredGenres) {
        return rankServices(planType, maxBudget, videoQuality, devices, region, requireDownload, preferredGenres);
    }

    public void recommendStreamingServices() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Streaming Service Recommender!");

        // 1. Annual or Monthly Plan
        System.out.print("Annual or monthly plan? (Enter 'annual' or 'monthly'): ");
        String planType = scanner.nextLine().trim().toLowerCase();
        while (!planType.equals("monthly") && !planType.equals("annual")) {
            System.out.print("Invalid input. Please enter 'monthly' or 'annual': ");
            planType = scanner.nextLine().trim().toLowerCase();
        }

        // 2. Maximum Budget
        System.out.print("What is your maximum budget (in dollars) for the " + planType + " plan? ");
        double maxBudget;
        try {
            maxBudget = Double.parseDouble(scanner.nextLine().trim());
            if (maxBudget <= 0) {
                throw new IllegalArgumentException("Budget must be positive.");
            }
        } catch (Exception e) {
            System.out.println("Invalid budget. Using default budget of $20.");
            maxBudget = 20.0;
        }

        // 3. Video Quality
        System.out.print("What video quality do you prefer? (e.g., HD, Full HD, 4K UHD): ");
        String videoQuality = scanner.nextLine().trim();

        // 4. Simultaneous Streaming Devices
        System.out.print("How many simultaneous streaming devices do you need? ");
        int devices;
        try {
            devices = Integer.parseInt(scanner.nextLine().trim());
            if (devices <= 0) {
                throw new IllegalArgumentException("Number of devices must be positive.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Using default of 1 device.");
            devices = 1;
        }

        // 5. Region Availability with Spell Check
        System.out.println("Which region are you in? (We'll use spell check to suggest options): ");
        String region = null;
        while (region == null) {
            System.out.print("Region: ");
            String regionInput = scanner.nextLine().trim();
            region = selectRegionFromSuggestions(regionInput);
            if (region == null) {
                System.out.println("Please select a valid region to continue.");
            }
        }

        // 6. Offline Download
        System.out.print("Do you want offline download? (Yes/No): ");
        String downloadInput = scanner.nextLine().trim().toLowerCase();
        boolean requireDownload;
        while (!downloadInput.equals("yes") && !downloadInput.equals("no")) {
            System.out.print("Invalid input. Please enter 'Yes' or 'No': ");
            downloadInput = scanner.nextLine().trim().toLowerCase();
        }
        requireDownload = downloadInput.equals("yes");

        // 7. Genres with Word Completion
        List<String> preferredGenres = new ArrayList<>();
        System.out.println("Enter the genres you would like to watch (one at a time, type 'done' to finish). We'll use word completion to suggest options.");
        while (true) {
            System.out.print("Genre: ");
            String genreInput = scanner.nextLine().trim().toLowerCase();
            if (genreInput.equals("done")) {
                break;
            }
            String selectedGenre = selectGenreFromSuggestions(genreInput);
            if (selectedGenre != null && !preferredGenres.contains(selectedGenre)) {
                preferredGenres.add(selectedGenre);
            }
        }

        // Ensure there are preferred genres
        if (preferredGenres.isEmpty()) {
            System.out.println("No valid genres provided. Using default genres: drama, comedy.");
            preferredGenres.add("drama");
            preferredGenres.add("comedy");
        }

        // 8. Confirm Preferences
        System.out.println("\nHere are your preferences:");
        System.out.println("- Plan Type: " + planType);
        System.out.println("- Maximum Budget: $" + maxBudget);
        System.out.println("- Video Quality: " + videoQuality);
        System.out.println("- Simultaneous Streaming Devices: " + devices);
        System.out.println("- Region: " + region);
        System.out.println("- Offline Download: " + (requireDownload ? "Yes" : "No"));
        System.out.println("- Genres: " + preferredGenres);
        System.out.print("\nDo you confirm these preferences? (Yes/No): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        while (!confirm.equals("yes") && !confirm.equals("no")) {
            System.out.print("Invalid input. Please enter 'Yes' or 'No': ");
            confirm = scanner.nextLine().trim().toLowerCase();
        }
        if (confirm.equals("no")) {
            System.out.println("Exiting due to unconfirmed preferences.");
            scanner.close();
            return;
        }

        // 9. Recommend Streaming Services
        System.out.println("\nBased on your preferences (Plan: " + planType + ", Budget: $" + maxBudget +
                ", Video Quality: " + videoQuality + ", Devices: " + devices + ", Region: " + region +
                ", Download: " + (requireDownload ? "Yes" : "No") + ", Genres: " + preferredGenres + "):");
        List<String> recommendations = getRecommendations(planType, maxBudget, videoQuality, devices, region, requireDownload, preferredGenres);
        if (recommendations.isEmpty()) {
            System.out.println("No streaming services match your criteria. Consider adjusting your preferences.");
        } else {
            System.out.println("Recommended Streaming Services:");
            for (String recommendation : recommendations) {
                System.out.println("- " + recommendation);
            }
        }

        scanner.close();
    }
    /// Normalize genre names for comparison
    private String normalizeGenre(String genre) {
        if (genre.contains("action")) return "action";
        if (genre.contains("sci-fi") || genre.contains("science fiction")) return "sci-fi";
        if (genre.contains("documentary") || genre.contains("documentaries")) return "documentary";
        if (genre.contains("kids")) return "kids";
        if (genre.contains("mystery")) return "mystery";
        return genre;
    }

    public static void main(String[] args) {
        StreamingRecommender recommender = new StreamingRecommender();
        recommender.recommendStreamingServices();
    }
}