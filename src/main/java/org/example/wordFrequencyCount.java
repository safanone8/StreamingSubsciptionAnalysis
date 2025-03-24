package org.example;

import java.io.*;
import java.util.*;

public class wordFrequencyCount {
	public static Map<String, Integer> getGenreFrequencies() {
		Map<String, Integer> genreCount = new HashMap<>();
		File dataDir = new File("data");
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			System.err.println("Data directory 'data' not found or is not a directory.");
			return genreCount;
		}

		File[] csvFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
		if (csvFiles == null || csvFiles.length == 0) {
			System.err.println("No CSV files found in the 'data' directory.");
			return genreCount;
		}

		for (File csvFile : csvFiles) {
			String filePath = csvFile.getPath();
			countGenreFrequencies(filePath, genreCount);
		}
		return genreCount;
	}

	public static Map<String, StreamingRecommender.ServiceData> loadServiceData() {
		Map<String, StreamingRecommender.ServiceData> serviceDataMap = new HashMap<>();
		File dataDir = new File("data");
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			System.err.println("Data directory 'data' not found or is not a directory.");
			return serviceDataMap;
		}

		File[] csvFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
		if (csvFiles == null || csvFiles.length == 0) {
			System.err.println("No CSV files found in the 'data' directory.");
			return serviceDataMap;
		}

		for (File csvFile : csvFiles) {
			String filePath = csvFile.getPath();
			System.out.println("Processing file: " + filePath);
			processFile(filePath, serviceDataMap);
		}

		return serviceDataMap;
	}

	private static void processFile(String filePath, Map<String, StreamingRecommender.ServiceData> serviceDataMap) {
		String serviceName = new File(filePath).getName().replace(".csv", "");
		serviceName = switch (serviceName.toLowerCase()) {
			case "britbox" -> "BritBox";
			case "cbcgem_data" -> "CBC Gem";
			case "crave_data" -> "Crave";
			case "prime_video_data" -> "Prime Video";
			case "paramount_plus_data" -> "Paramount Plus";
			default -> serviceName;
		};

		StreamingRecommender.ServiceData serviceData = new StreamingRecommender.ServiceData(serviceName);
		serviceDataMap.put(serviceName, serviceData);

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			boolean isFirstLine = true;
			int genreColumnIndex = -1;
			int monthlyPlanIndex = -1;
			int annualPlanIndex = -1;
			int nameIndex = -1;
			int qualityIndex = -1;
			int devicesIndex = -1;
			int adIndex = -1;
			int downloadIndex = -1;
			int regionIndex = -1;

			String currentPlanName = "Default";
			String currentQuality = "";
			int currentDevices = 0;
			boolean currentAd = false;
			boolean currentDownload = false;
			List<String> currentRegions = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				if (isFirstLine) {
					String[] headers = parseCsvLine(line);
					for (int i = 0; i < headers.length; i++) {
						String header = headers[i].trim().toLowerCase();
						if (header.equals("genre") || header.equals("category")) {
							genreColumnIndex = i;
						} else if (header.contains("monthly plan")) {
							monthlyPlanIndex = i;
						} else if (header.contains("annual plan") || header.contains("anually plan")) {
							annualPlanIndex = i;
						} else if (header.equals("name")) {
							nameIndex = i;
						} else if (header.equals("quality")) {
							qualityIndex = i;
						} else if (header.equals("devices") || header.equals("divices")) {
							devicesIndex = i;
						} else if (header.equals("ad")) {
							adIndex = i;
						} else if (header.equals("download")) {
							downloadIndex = i;
						} else if (header.contains("regional availability") || header.contains("regional availibility")) {
							regionIndex = i;
						}
					}
					if (genreColumnIndex == -1) {
						System.err.println("Genre column not found in file: " + filePath);
						return;
					}
					isFirstLine = false;
					continue;
				}

				String[] columns = parseCsvLine(line);
				if (columns.length <= genreColumnIndex) {
					continue;
				}

				// Update plan name if provided
				if (nameIndex != -1 && columns.length > nameIndex && !columns[nameIndex].trim().isEmpty()) {
					currentPlanName = columns[nameIndex].trim();
				}

				// Update quality, devices, ads, download, regions if provided
				if (qualityIndex != -1 && columns.length > qualityIndex && !columns[qualityIndex].trim().isEmpty()) {
					currentQuality = columns[qualityIndex].trim();
				}
				if (devicesIndex != -1 && columns.length > devicesIndex && !columns[devicesIndex].trim().isEmpty()) {
					String devicesStr = columns[devicesIndex].trim().replaceAll("[^0-9]", "");
					try {
						currentDevices = Integer.parseInt(devicesStr);
					} catch (NumberFormatException e) {
						currentDevices = 0;
					}
				}
				if (adIndex != -1 && columns.length > adIndex && !columns[adIndex].trim().isEmpty()) {
					currentAd = columns[adIndex].trim().equalsIgnoreCase("Yes");
				}
				if (downloadIndex != -1 && columns.length > downloadIndex && !columns[downloadIndex].trim().isEmpty()) {
					currentDownload = columns[downloadIndex].trim().equalsIgnoreCase("Yes");
				}
				if (regionIndex != -1 && columns.length > regionIndex && !columns[regionIndex].trim().isEmpty()) {
					String regionsStr = columns[regionIndex].trim();
					currentRegions = Arrays.asList(regionsStr.split(",\\s*"));
					serviceData.regions = new ArrayList<>(currentRegions);
				}

				// Process monthly plan
				if (monthlyPlanIndex != -1 && columns.length > monthlyPlanIndex && !columns[monthlyPlanIndex].trim().isEmpty()) {
					String monthlyPlan = columns[monthlyPlanIndex].trim().replace("$", "");
					try {
						double cost = Double.parseDouble(monthlyPlan);
						String planName = (nameIndex != -1 && columns.length > nameIndex && !columns[nameIndex].trim().isEmpty())
								? columns[nameIndex].trim()
								: "Default Monthly";
						serviceData.monthlyPlans.put(planName, cost);
						serviceData.planQualities.put(planName, currentQuality);
						serviceData.planDevices.put(planName, currentDevices);
				//		serviceData.planAds.put(planName, currentAd);
						serviceData.planDownloads.put(planName, currentDownload);
					} catch (NumberFormatException e) {
						// Skip invalid numbers
					}
				}

				// Process annual plan
				if (annualPlanIndex != -1 && columns.length > annualPlanIndex && !columns[annualPlanIndex].trim().isEmpty()) {
					String annualPlan = columns[annualPlanIndex].trim().replace("$", "");
					try {
						double cost = Double.parseDouble(annualPlan);
						String planName = (nameIndex != -1 && columns.length > nameIndex && !columns[nameIndex].trim().isEmpty())
								? columns[nameIndex].trim()
								: "Default Annual";
						serviceData.yearlyPlans.put(planName, cost);
						serviceData.planQualities.put(planName, currentQuality);
						serviceData.planDevices.put(planName, currentDevices);
					//	serviceData.planAds.put(planName, currentAd);
						serviceData.planDownloads.put(planName, currentDownload);
					} catch (NumberFormatException e) {
						// Skip invalid numbers
					}
				}

				// Process genre
				String genre = columns[genreColumnIndex].trim();
				if (!genre.isEmpty()) {
					genre = normalizeGenre(genre.toLowerCase());
					if (!serviceData.genres.contains(genre)) {
						serviceData.genres.add(genre);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading file " + filePath + ": " + e.getMessage());
		}
	}

	private static void countGenreFrequencies(String filePath, Map<String, Integer> genreCount) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			boolean isFirstLine = true;
			int genreColumnIndex = -1;

			while ((line = reader.readLine()) != null) {
				if (isFirstLine) {
					String[] headers = parseCsvLine(line);
					for (int i = 0; i < headers.length; i++) {
						if (headers[i].trim().equalsIgnoreCase("Genre") || headers[i].trim().equalsIgnoreCase("category")) {
							genreColumnIndex = i;
							break;
						}
					}
					if (genreColumnIndex == -1) {
						System.err.println("Genre column not found in file: " + filePath);
						return;
					}
					isFirstLine = false;
					continue;
				}

				String[] columns = parseCsvLine(line);
				if (columns.length <= genreColumnIndex) {
					continue;
				}

				String genre = columns[genreColumnIndex].trim();
				if (genre.isEmpty()) {
					continue;
				}

				genre = normalizeGenre(genre.toLowerCase());
				genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
			}
		} catch (IOException e) {
			System.err.println("Error reading file " + filePath + ": " + e.getMessage());
		}
	}

	private static String normalizeGenre(String genre) {
		if (genre.contains("action")) return "action";
		if (genre.contains("sci-fi") || genre.contains("science fiction")) return "sci-fi";
		if (genre.contains("documentary") || genre.contains("documentaries")) return "documentary";
		if (genre.contains("kids")) return "kids";
		if (genre.contains("mystery")) return "mystery";
		return genre;
	}

	private static String[] parseCsvLine(String line) {
		List<String> columns = new ArrayList<>();
		boolean inQuotes = false;
		StringBuilder field = new StringBuilder();

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				columns.add(field.toString().replace("\"", ""));
				field = new StringBuilder();
			} else {
				field.append(c);
			}
		}
		columns.add(field.toString().replace("\"", ""));
		return columns.toArray(new String[0]);
	}
}