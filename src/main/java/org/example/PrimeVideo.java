package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PrimeVideo {
    public static void scrape() {
        System.setProperty("webdriver.chrome.driver", "D:/Downloads/EXE/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        WebDriver browser = new ChromeDriver();
        WebDriverWait waiter = new WebDriverWait(browser, Duration.ofSeconds(10));

        try {
            browser.get("https://www.primevideo.com/");
            browser.manage().window().maximize();
            Thread.sleep(3000);
            List<String[]> plansData = extractPlans(browser, waiter);
            List<String> categories = clickCategoriesButton(browser, waiter);
            exportToCSV(plansData, categories);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            browser.quit();
        }
    }

    private static List<String> clickCategoriesButton(WebDriver browser, WebDriverWait waiter) {
        List<String> categories = new ArrayList<>();
        WebElement categoriesButton = waiter.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[aria-label='Categories']")));
        categoriesButton.click();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        WebElement dropdown = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.K0Kf63._0B0vPr")));
        List<WebElement> genreElements = dropdown.findElements(By.cssSelector("h3.bUIMWe"));
        for (WebElement genreElement : genreElements) {
            String category = genreElement.findElement(By.cssSelector("span.tjl5-c > span")).getText().trim();
            if (category != null && !category.trim().isEmpty()) categories.add(category);
        }
        return categories;
    }

    private static List<String[]> extractPlans(WebDriver browser, WebDriverWait waiter) {
        List<String[]> plansData = new ArrayList<>();
        WebElement planElement = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span._body_1qfim_94")));
        String planText = planElement.getText();
        String monthlyPrice = "";
        String annualPrice = "";
        int monthlyStart = planText.indexOf("Pay $");
        if (monthlyStart != -1) {
            int monthlyEnd = planText.indexOf("/", monthlyStart + 5);
            monthlyPrice = planText.substring(monthlyStart + 4, monthlyEnd);
            plansData.add(new String[]{"Monthly Plan", monthlyPrice});
        }
        int annualStart = planText.indexOf("($");
        if (annualStart != -1) {
            int annualEnd = planText.indexOf("/", annualStart + 2);
            annualPrice = planText.substring(annualStart + 1, annualEnd);
            plansData.add(new String[]{"Annual Plan", annualPrice});
        }
        return plansData;
    }

    private static void exportToCSV(List<String[]> plansData, List<String> categories) {
        String csvFile = "prime_video_data.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            // Updated header with new columns
            writer.append("Monthly Plan,Annual Plan,Genre,Quality,Devices,Download,Regional Availibility\n");
            String monthlyPrice = "";
            String annualPrice = "";
            for (String[] plan : plansData) {
                if (plan[0].equals("Monthly Plan")) monthlyPrice = plan[1];
                if (plan[0].equals("Annual Plan")) annualPrice = plan[1];
            }
            String firstGenre = categories.isEmpty() ? "" : categories.get(0);
            
            // First row with all data
            String quality = "\"4K UHD, Full HD, HD\"";
            String devices = "3 Devices";
            String download = "yes";
            String regionalAvailability = "\"United States, United Kingdom, Germany, Austria, Japan, Belgium, Brazil, Canada, France, India, Ireland, Italy, Poland, Turkey, Spain, Mexico, Argentina, Chile, Colombia, Costa Rica, New Zealand, South Africa,Finland, Denmark, Norway, Iceland, Australia, Netherlands, Saudi Arabia, Luxembourg, Portugal, Indonesia, Philippines, Thailand, Nigeria, South Africa, Latvia, Switzerland, Egypt, Guernsey, Cameroon, Nicaragua, Slovenia, Canada\"";
            
            writer.append(String.format("\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",%s\n",
                monthlyPrice, annualPrice, firstGenre, quality, devices, download, regionalAvailability));
            
            // Remaining rows with only genres
            for (int i = 1; i < categories.size(); i++) {
                writer.append(String.format(",,\"%s\",,,,\n", categories.get(i)));
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        scrape();
    }
}