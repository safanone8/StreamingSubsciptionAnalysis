package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScrapeAndRecommend {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize StreamingRecommender with pre-existing data
        StreamingRecommender recommender = new StreamingRecommender();
        Scanner scanner = new Scanner(System.in);

        // Print formatted welcome message in ASCII style
        System.out.println("\033[1;33m" + "  __        __   _                            " + "\033[0m");
        System.out.println("\033[1;33m" + "  \\ \\      / /__| | ___ ___  _ __ ___   ___  " + "\033[0m");
        System.out.println("\033[1;33m" + "   \\ \\ /\\ / / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\ " + "\033[0m");
        System.out.println("\033[1;33m" + "    \\ V  V /  __/ | (_| (_) | | | | | |  __/ " + "\033[0m");
        System.out.println("\033[1;33m" + "     \\_/\\_/ \\___|_|\\___\\___/|_| |_| |_|\\___| " + "\033[0m");
        System.out.println("\033[1;34m" + "===============================================" + "\033[0m\n");
        System.out.println("\033[1;32m" + "  Welcome to the Streaming Service Recommender" + "\033[0m");
        System.out.println("\033[1;32m" + "                                  " + "\033[0m");
        System.out.println("\033[1;34m" + "===============================================" + "\033[0m");


        // Collect user preferences
        System.out.print("Annual or monthly plan? (Enter 'annual' or 'monthly'): ");
        String planType = scanner.nextLine().trim().toLowerCase();
        while (!planType.equals("monthly") && !planType.equals("annual")) {
            System.out.print("Invalid input. Please enter 'monthly' or 'annual': ");
            planType = scanner.nextLine().trim().toLowerCase();
        }

        System.out.print("What is your maximum budget (in dollars) for the " + planType + " plan? ");
        double maxBudget;
        try {
            maxBudget = Double.parseDouble(scanner.nextLine().trim());
            if (maxBudget <= 0) throw new IllegalArgumentException("Budget must be positive.");
        } catch (Exception e) {
            System.out.println("Invalid budget. Using default budget of $20.");
            maxBudget = 20.0;
        }

        System.out.print("What video quality do you prefer? (e.g., HD, Full HD, 4K UHD): ");
        String videoQuality = scanner.nextLine().trim();

        System.out.print("How many simultaneous streaming devices do you need? ");
        int devices;
        try {
            devices = Integer.parseInt(scanner.nextLine().trim());
            if (devices <= 0) throw new IllegalArgumentException("Number of devices must be positive.");
        } catch (Exception e) {
            System.out.println("Invalid input. Using default of 1 device.");
            devices = 1;
        }

        System.out.println("Which region are you in?: ");
        String region = null;
        while (region == null) {
            System.out.print("Region: ");
            String regionInput = scanner.nextLine().trim();
            region = recommender.selectRegionFromSuggestions(regionInput);
            if (region == null) System.out.println("Please select a valid region to continue.");
        }

        System.out.print("Do you want offline download? (Yes/No): ");
        String downloadInput = scanner.nextLine().trim().toLowerCase();
        boolean requireDownload;
        while (!downloadInput.equals("yes") && !downloadInput.equals("no")) {
            System.out.print("Invalid input. Please enter 'Yes' or 'No': ");
            downloadInput = scanner.nextLine().trim().toLowerCase();
        }
        requireDownload = downloadInput.equals("yes");

        List<String> preferredGenres = new ArrayList<>();
        System.out.println("Enter the genres you would like to watch (one at a time, type 'done' to finish).");
        while (true) {
            System.out.print("Genre: ");
            String genreInput = scanner.nextLine().trim().toLowerCase();
            if (genreInput.equals("done")) break;
            String selectedGenre = recommender.selectGenreFromSuggestions(genreInput);
            if (selectedGenre != null && !preferredGenres.contains(selectedGenre)) {
                preferredGenres.add(selectedGenre);
            }
        }
        if (preferredGenres.isEmpty()) {
            System.out.println("No valid genres provided. Using default genres: drama, comedy.");
            preferredGenres.add("drama");
            preferredGenres.add("comedy");
        }

        // Confirm preferences
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

        // Perform scraping (for demonstration)
        System.out.println("Scraping data from streaming services...");
        BritBox.scrape();
        Crave.scrape();
        CBCGem.scrape();
        PrimeVideo.scrape();
        ParamountPlusScraper.scrape();
        // Add other services if applicable, e.g., ParamountPlus.scrape();
        System.out.println("Data scraped.");

        // Get recommendations using pre-existing data (loaded at instantiation)
        List<String> recommendations = recommender.getRecommendations(
                planType, maxBudget, videoQuality, devices, region, requireDownload, preferredGenres
        );

        // Print recommendations
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
}