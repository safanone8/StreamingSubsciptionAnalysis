package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CBCGem {
    public static void scrape() throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver", "D:/Downloads/EXE/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        File csvFile = new File("CBCGem_Data.csv");
        FileWriter fileWriter = new FileWriter(csvFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        try {
            // Updated header with new columns
            bufferedWriter.write("Monthly Plan,Genre,Quality,Devices,Download,Regional Availability\n");
            driver.get("https://gem.cbc.ca/browse");
            driver.manage().window().maximize();
            dismissCookiePopup(driver, wait);
            scrollDownPage(driver);
            List<String> genres = scrapeGenres(driver);

            driver.get("https://gem.cbc.ca/login");
            dismissCookiePopup(driver, wait);
            WebElement signInWithCBCButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.sc-9fc15cf3-0.igMawT")));
            signInWithCBCButton.click();
            WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
            emailField.sendKeys("zeelparekh1910@gmail.com");
            WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
            continueButton.click();
            WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
            passwordField.sendKeys("zeel@1910");
            WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("next")));
            signInButton.click();
            Thread.sleep(5000);
            WebElement tryPremiumLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.sc-9fc15cf3-1.jOTRMd")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tryPremiumLink);
            Thread.sleep(2000);
            scrollDownPage(driver);
            WebElement tryPremiumButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.sc-9fc15cf3-0.XixyA")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tryPremiumButton);
            Thread.sleep(2000);
            scrollDownPage(driver);
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.sc-9fc15cf3-0.igMawT")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);
            Thread.sleep(2000);
            scrollDownPage(driver);
            List<String> plans = scrapeAllPlans(driver);

            for (int i = 0; i < genres.size(); i++) {
                String plan = (i == 0 && !plans.isEmpty()) ? plans.get(0) : "";  // Only show plan in first row
                String category = genres.get(i);
                // Hardcoded values for new columns, only shown in first row
                String quality = (i == 0) ? "\"Full HD, HD\"" : "";
                String devices = (i == 0) ? "3 devices" : "";
                String download = (i == 0) ? "Yes" : "";
                String regionalAvailability = (i == 0) ? "Canada" : "";

                bufferedWriter.write(String.format("\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\"\n",
                    plan.replace("\"", "\"\""),
                    category.replace("\"", "\"\""),
                    quality,
                    devices,
                    download,
                    regionalAvailability));
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        } finally {
            bufferedWriter.close();
            driver.quit();
        }
    }

    private static void dismissCookiePopup(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Close notification']")));
            closeButton.click();
        } catch (Exception ignored) {}
    }

    private static void scrollDownPage(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");
        long scrollStep = pageHeight / 10;
        int scrollDuration = 5000;
        int steps = 10;
        for (int i = 0; i < steps; i++) {
            js.executeScript("window.scrollBy(0, " + scrollStep + ");");
            Thread.sleep(scrollDuration / steps);
        }
        Thread.sleep(2000);
    }

    private static List<String> scrapeGenres(WebDriver driver) {
        List<String> genres = new ArrayList<>();
        try {
            WebElement genreList = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.sc-8eb0eec9-1.imPJSH")));
            List<WebElement> genreElements = driver.findElements(By.cssSelector("li.sc-8eb0eec9-2.iHvVQi"));
            for (WebElement genre : genreElements) {
                String genreName = genre.findElement(By.cssSelector("a[href^='/category/']")).getText().trim();
                if (!genreName.isEmpty() && !genres.contains(genreName)) genres.add(genreName);
            }
        } catch (Exception e) {
            System.out.println("Error scraping genres: " + e.getMessage());
        }
        return genres;
    }

    private static List<String> scrapeAllPlans(WebDriver driver) {
        List<String> plans = new ArrayList<>();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.sc-bfe59bfc-0.cEnRCx")));
            WebElement priceElement = driver.findElement(By.cssSelector("p.sc-e5b438da-0.sc-e5b438da-3.sc-bfe59bfc-1.eTknzQ.fVKdyV.dQklfo"));
            String priceDetails = priceElement.getText().replace("\n", " ").trim();
            String price = priceDetails.replaceAll(".*(\\$\\d+\\.\\d{2}).*", "$1");
            if (!price.equals("N/A")) plans.add(price);
        } catch (Exception e) {
            System.out.println("Error scraping plans: " + e.getMessage());
        }
        return plans;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        scrape();
    }
}