package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BritBox {
    public static void scrape() {
        System.setProperty("webdriver.chrome.driver", "D:/Downloads/EXE/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get("https://www.britbox.com/ca/");
            WebElement startWatchingButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Start Watching Now")));
            startWatchingButton.click();
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            emailInput.sendKeys("kdpgami5170@gmail.com");
            WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.button[type='submit']")));
            continueButton.click();
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pwd")));
            passwordInput.sendKeys("#Kdp12me028");

            // Locate the Sign Up button
            WebElement signUpButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

            // Retry logic for clicking Sign Up button
            int maxAttempts = 3; // Maximum number of retry attempts
            int attempt = 1;
            boolean redirected = false;

            while (attempt <= maxAttempts && !redirected) {
                System.out.println("Attempt " + attempt + ": Clicking Sign Up button...");
                signUpButton.click();
                System.out.println("Clicked Sign Up button (Attempt " + attempt + ")");

                // Wait for 1 second to check if redirection happens
                try {
                    wait.withTimeout(Duration.ofSeconds(2)).until(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Choose Your Plan')]"))
                    );
                    System.out.println("Page redirected successfully after attempt " + attempt + ".");
                    redirected = true;
                } catch (Exception e) {
                    System.out.println("Page did not redirect after attempt " + attempt + " within 1 second.");
                    if (attempt < maxAttempts) {
                        // Re-locate the Sign Up button in case the page state changed
                        try {
                            signUpButton = driver.findElement(By.cssSelector("button[type='submit']"));
                        } catch (Exception ex) {
                            System.out.println("Sign Up button no longer present, assuming redirection occurred.");
                            redirected = true;
                        }
                    }
                }
                attempt++;
            }

            if (!redirected) {
                throw new Exception("Failed to redirect to the plans page after " + maxAttempts + " attempts. Please check the website behavior or network connection.");
            }

            // Proceed with scraping plans
            List<String> genres = new ArrayList<>();
            List<String> monthlyPrices = new ArrayList<>();
            List<String> annualPrices = new ArrayList<>();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Choose Your Plan')]")));
            List<WebElement> planLabels = driver.findElements(By.className("group-checkbox"));
            for (WebElement planLabel : planLabels) {
                String price = planLabel.findElement(By.cssSelector("h4.price b")).getText();
                String billingPeriod = planLabel.findElement(By.className("price-caption")).getText();
                if (billingPeriod.toLowerCase().contains("month")) {
                    monthlyPrices.add(price);
                } else if (billingPeriod.toLowerCase().contains("year")) {
                    annualPrices.add(price);
                }
            }

            WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img.logo.cursor-pointer[alt='BritBox']")));
            logo.click();
            WebElement exploreButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-menu='data-explore']")));
            exploreButton.click();
            WebElement genreBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-tab='Genre']")));
            List<WebElement> genreLinks = genreBlock.findElements(By.cssSelector("#Genre-tab ul li a"));
            for (WebElement link : genreLinks) {
                String genreName = link.getText().trim();
                if (!genreName.isEmpty()) genres.add(genreName);
            }

            String csvFilePath = "britbox.csv";
            try (FileWriter writer = new FileWriter(csvFilePath)) {
                File dir = new File("");
                if (!dir.exists()) dir.mkdir();
                writer.append("Monthly Plan,Annual Plan,Genre,Quality,Devices,Ad,Download,Regional Availability\n");
                int maxRows = genres.size();
                for (int i = 0; i < maxRows; i++) {
                    String monthlyPrice = (i == 0 && !monthlyPrices.isEmpty()) ? monthlyPrices.get(0).replace("/month", "").replace(" CAD", "").replaceAll("\\.\\d{2}", "") : "";
                    String annualPrice = (i == 0 && !annualPrices.isEmpty()) ? annualPrices.get(0).replace("/year", "").replace(" CAD", "").replaceAll("\\.\\d{2}", "") : "";
                    String genre = i < genres.size() ? genres.get(i) : "";
                    String quality = (i == 0) ? "\"4K UHD, Full HD, HD\"" : "";
                    String devices = (i == 0) ? "4 devices" : "";
                    String ad = (i == 0) ? "No" : "";
                    String download = (i == 0) ? "Yes" : "";
                    String regionalAvailability = (i == 0) ? "\"United States, Canada, Australia, Denmark, Finland, Iceland, Norway, Sweden\"" : "";
                    
                    writer.append(String.format("\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",%s\n", 
                        monthlyPrice, annualPrice, genre, quality, devices, ad, download, regionalAvailability));
                }
            } catch (IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        scrape();
    }
}