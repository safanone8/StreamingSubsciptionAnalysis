package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

public class Crave {
    public static void scrape() {
        WebDriver driver = initDriver();
        if (driver == null) return;
        PageScraper scraper = new PageScraper(driver);
        scraper.scrapeAllData();
        driver.quit();
    }

    public static WebDriver initDriver() {
        try {
            System.setProperty("webdriver.chrome.driver", "D:/Downloads/EXE/chromedriver-win64/chromedriver-win64/chromedriver.exe");
            return new ChromeDriver();
        } catch (Exception e) {
            System.err.println("Error initializing WebDriver: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        scrape();
    }

    static class PageScraper {
        private WebDriver driver;
        public PageScraper(WebDriver driver) { this.driver = driver; }
        public void scrapeAllData() {
            String csvFilePath = "crave_data.csv";
            try (FileWriter writer = new FileWriter(csvFilePath)) {
                File dir = new File("");
                if (!dir.exists()) dir.mkdir();
                List<String> categories = scrapeCategories();
                List<List<String>> plans = scrapePlans();
                writeToCSV(writer, categories, plans.get(0), plans.get(1), plans.get(2));
            } catch (IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            }
        }
        private List<String> scrapeCategories() {
            List<String> categoryList = new ArrayList<>();
            driver.get("https://www.crave.ca/en/movies/all-movies");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement filterButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.GridFilterstyles__ButtonToggle-sc-1ytia0z-5[data-testid='filter-toggle']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterButton);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".GridFilterstyles__FilterList-sc-1ytia0z-15")));
            List<WebElement> categories = driver.findElements(By.cssSelector(".GridFilterstyles__FilterList-sc-1ytia0z-15 li span[role='checkbox']"));
            for (WebElement category : categories) {
                String categoryText = category.getText().trim();
                if (!categoryText.isEmpty()) categoryList.add(categoryText);
            }
            return categoryList;
        }
        private List<List<String>> scrapePlans() {
            List<List<String>> allPlans = new ArrayList<>();
            List<String> monthlyPlans = new ArrayList<>();
            List<String> annualPlans = new ArrayList<>();
            List<String> bundlePlans = new ArrayList<>();
            driver.get("https://www.crave.ca/en/subscribe");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".CravePDPstyles__PackagesWrapper-sc-5nckjv-29")));
            scrapePlanDetails(wait, monthlyPlans);
            WebElement annualTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[data-index='2']")));
            js.executeScript("arguments[0].click();", annualTab);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".CravePDPstyles__PackagesWrapper-sc-5nckjv-29")));
            scrapePlanDetails(wait, annualPlans);
            WebElement bundleTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[data-index='3']")));
            js.executeScript("arguments[0].click();", bundleTab);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".CravePDPstyles__PackagesWrapper-sc-5nckjv-29")));
            scrapePlanDetails(wait, bundlePlans);
            allPlans.add(monthlyPlans);
            allPlans.add(annualPlans);
            allPlans.add(bundlePlans);
            return allPlans;
        }
        private void scrapePlanDetails(WebDriverWait wait, List<String> planList) {
            List<WebElement> planPanels = driver.findElements(By.cssSelector(".CravePDPstyles__PlanPanel-sc-5nckjv-40"));
            for (WebElement panel : planPanels) {
                WebElement img = panel.findElement(By.tagName("img"));
                String planName = img.getDomAttribute("alt").replace("Crave ", "").replace(" Ad-free", "").replace(" with Ads", "");
                WebElement priceElement = panel.findElement(By.cssSelector(".CravePDPstyles__Price-sc-5nckjv-104"));
                WebElement billingCycle = panel.findElement(By.cssSelector(".CravePDPstyles__BillingCycle-sc-5nckjv-105"));
                String price = priceElement.getText() + billingCycle.getText();
                planList.add(planName + " - " + price);
            }
        }
        private void writeToCSV(FileWriter writer, List<String> categories, List<String> monthlyPlans, List<String> annualPlans, List<String> bundlePlans) throws IOException {
            // Updated header with new columns
            writer.append("Name,Monthly plan,Anually plan,Genre,Quality,Divices,Download,Regional Availability\n");
            int maxRows = Math.max(Math.max(monthlyPlans.size(), annualPlans.size()), categories.size());
            for (int i = 0; i < maxRows; i++) {
                String monthlyPlan = (i < monthlyPlans.size()) ? monthlyPlans.get(i) : "";
                String planName = monthlyPlan.contains(" - ") ? monthlyPlan.split(" - ")[0].trim() : "";
                String monthlyPrice = monthlyPlan.contains(" - ") ? monthlyPlan.split(" - ")[1].trim().replace("/mo", "") : "";
                String annualPlan = (i < annualPlans.size()) ? annualPlans.get(i) : "";
                String annualPrice = annualPlan.contains(" - ") ? annualPlan.split(" - ")[1].trim().replace("/yr", "") : "";
                String category = (i < categories.size()) ? categories.get(i) : "";
                
                // Hardcoded values for new columns based on plan type
                String quality = "";
                String devices = "";
                String download = "";
                String regionalAvailability = "";
                
                if (i == 0 && !planName.isEmpty()) {  // Premium
                    quality = "\"4K UHD, Full HD, HD\"";
                    devices = "4 devices";
                    download = "Yes";
                    regionalAvailability = "Canada";
                } else if (i == 1 && !planName.isEmpty()) {  // Standard
                    quality = "\"4K UHD, Full HD, HD\"";
                    devices = "4 devices";
                    download = "No";
                    regionalAvailability = "";
                } else if (i == 2 && !planName.isEmpty()) {  // Basic
                    quality = "HD";
                    devices = "1 devices";
                    download = "No";
                    regionalAvailability = "";
                }

                writer.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\"\n",
                    planName, monthlyPrice, annualPrice, category,
                    quality, devices, download, regionalAvailability));
            }
        }
    }
}