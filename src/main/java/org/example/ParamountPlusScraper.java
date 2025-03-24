package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ParamountPlusScraper {
    public static void scrape() {
        System.setProperty("webdriver.chrome.driver", "D:/Downloads/EXE/chromedriver-win64/chromedriver-win64/chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get("https://www.paramountplus.com/ca/");
            driver.manage().window().maximize();
            System.out.println("Paramount Plus Canada Opened");

            WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("js-li-sign-in")));
            signInButton.click();
            System.out.println("Clicked Sign In button");

            Thread.sleep(2000);

            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            emailField.sendKeys("safanvhora8@gmail.com");
            System.out.println("Entered email");

            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            passwordField.sendKeys("@Safanvhora77");
            System.out.println("Entered password");

            // Click the Continue button with retry logic
            WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("qt-continuebtn")));

            int maxAttempts = 3;
            int attempt = 1;
            boolean redirected = false;

            while (attempt <= maxAttempts && !redirected) {
                System.out.println("Attempt " + attempt + ": Clicking Continue button...");
                continueButton.click();
                System.out.println("Clicked Continue button (Attempt " + attempt + ")");

                Thread.sleep(2000);

                try {
                    wait.withTimeout(Duration.ofSeconds(2)).until(ExpectedConditions.presenceOfElementLocated(By.className("interstitial_button")));
                    System.out.println("Page redirected successfully after attempt " + attempt + ".");
                    redirected = true;
                } catch (Exception e) {
                    System.out.println("Page did not redirect after attempt " + attempt + ".");
                    if (attempt < maxAttempts) {
                        try {
                            continueButton = driver.findElement(By.className("qt-continuebtn"));
                        } catch (Exception ex) {
                            System.out.println("Continue button no longer present, assuming redirection occurred.");
                            redirected = true;
                        }
                    }
                }
                attempt++;
            }

            if (!redirected) {
                throw new Exception("Failed to redirect to the interstitial page after " + maxAttempts + " attempts. Please check the website behavior or network connection.");
            }

            // Click the Next Continue button with retry logic
            WebElement interstitialElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("interstitial_button")));
            WebElement nextContinueButton = interstitialElement.findElement(By.className("buttonWindows"));

            attempt = 1;
            redirected = false;

            while (attempt <= maxAttempts && !redirected) {
                System.out.println("Attempt " + attempt + ": Clicking Next Continue button...");
                nextContinueButton.click();
                System.out.println("Clicked Next Continue button (Attempt " + attempt + ")");

                Thread.sleep(2000);

                try {
                    wait.withTimeout(Duration.ofSeconds(2)).until(ExpectedConditions.presenceOfElementLocated(By.id("main-aa-container")));
                    System.out.println("Page redirected successfully after attempt " + attempt + ".");
                    redirected = true;
                } catch (Exception e) {
                    System.out.println("Page did not redirect after attempt " + attempt + ".");
                    if (attempt < maxAttempts) {
                        try {
                            interstitialElement = driver.findElement(By.className("interstitial_button"));
                            nextContinueButton = interstitialElement.findElement(By.className("buttonWindows"));
                        } catch (Exception ex) {
                            System.out.println("Interstitial element no longer present, assuming redirection occurred.");
                            redirected = true;
                        }
                    }
                }
                attempt++;
            }

            if (!redirected) {
                throw new Exception("Failed to redirect to the next page after " + maxAttempts + " attempts. Please check the website behavior or network connection.");
            }

            // Scrape plans from main-aa-container
            WebElement mainContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("main-aa-container")));
            System.out.println("Found main-aa-container");

            // Store plan data with additional details
            List<String[]> plansData = new ArrayList<>();

            // Find all plan elements using the 'plan' class
            List<WebElement> planElements = mainContainer.findElements(By.className("plan"));
            System.out.println("Found " + planElements.size() + " plan elements");

            for (WebElement planElement : planElements) {
                WebElement nameElement = planElement.findElement(By.xpath(".//div[contains(@class, 'plan-box__title')]//h3"));
                String planName = nameElement.getText().trim();
                System.out.println("Found plan name: " + planName);

                WebElement priceElement = planElement.findElement(By.xpath(".//div[contains(@class, 'plan-price__price')]//span[not(@id)]"));
                String monthlyPlan = priceElement.getText().trim();
                System.out.println("Found price for " + planName + ": " + monthlyPlan);

                String quality = "";
                String devices = "";
                String ad = "";
                String download = "";

                try {
                    WebElement featureList = planElement.findElement(By.className("feature-list"));
                    List<WebElement> features = featureList.findElements(By.xpath(".//li"));
                    System.out.println("Found " + features.size() + " features for " + planName);

                    System.out.println("Features for " + planName + ":");
                    for (int i = 0; i < features.size(); i++) {
                        System.out.println("Feature " + (i + 1) + ": " + features.get(i).getText().trim());
                    }

                    for (WebElement feature : features) {
                        String featureText = feature.getText().trim().toLowerCase();
                        System.out.println("Processing feature for " + planName + ": " + featureText);

                        if (featureText.contains("experience in")) {
                            quality = feature.getText().trim().replace("Experience in ", "");
                            System.out.println("Found quality for " + planName + ": " + quality);
                        } else if (featureText.contains("stream on") && featureText.contains("device")) {
                            devices = featureText.replace("stream on ", "").replace(" at a time", "").replace(" at once", "");
                            System.out.println("Found devices for " + planName + ": " + devices);
                        } else if (featureText.contains("ads") || featureText.contains("ad-free")) {
                            ad = featureText.contains("ad-free") ? "No" : "Yes";
                            System.out.println("Found ad for " + planName + ": " + ad);
                        } else if (featureText.contains("download")) {
                            download = featureText.contains("no") ? "No" : "Yes";
                            System.out.println("Found download for " + planName + ": " + download);
                        }
                    }

                    if (quality.isEmpty()) {
                        System.out.println("Quality not found for " + planName + ", defaulting to empty.");
                    }
                    if (devices.isEmpty()) {
                        System.out.println("Devices not found for " + planName + ", defaulting to empty.");
                    }
                    if (ad.isEmpty()) {
                        if (planName.toLowerCase().contains("basic")) {
                            ad = "Yes";
                        } else if (planName.toLowerCase().contains("standard") || planName.toLowerCase().contains("premium")) {
                            ad = "No";
                        }
                        System.out.println("Ad not found for " + planName + ", defaulting to: " + ad);
                    }
                    if (download.isEmpty()) {
                        if (planName.toLowerCase().contains("basic")) {
                            download = "No";
                        } else if (planName.toLowerCase().contains("standard") || planName.toLowerCase().contains("premium")) {
                            download = "Yes";
                        }
                        System.out.println("Download not found for " + planName + ", defaulting to: " + download);
                    }
                } catch (Exception e) {
                    System.out.println("Feature list not found for " + planName + ", defaulting to inferred values.");
                    if (planName.toLowerCase().contains("basic")) {
                        quality = "full HD";
                        devices = "1 device";
                        ad = "Yes";
                        download = "No";
                    } else if (planName.toLowerCase().contains("standard")) {
                        quality = "full HD";
                        devices = "2 devices";
                        ad = "No";
                        download = "Yes";
                    } else if (planName.toLowerCase().contains("premium")) {
                        quality = "4K UHD, HDR10, Dolby Vision & Dolby Atmos";
                        devices = "4 devices";
                        ad = "No";
                        download = "Yes";
                    }
                }

                if (!planName.isEmpty() && !monthlyPlan.isEmpty()) {
                    plansData.add(new String[]{planName, monthlyPlan, quality, devices, ad, download});
                }
            }

            if (plansData.isEmpty()) {
                System.out.println("Error: No plans were scraped from the website. Please check the webpage structure and selectors.");
            }

            System.out.println("Plans data:");
            for (String[] row : plansData) {
                System.out.println("[" + row[0] + ", " + row[1] + ", " + row[2] + ", " + row[3] + ", " + row[4] + ", " + row[5] + "]");
            }

            // Navigate to Movies page
            WebElement flexWrapper = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("flexWrapper")));
            System.out.println("Found flexWrapper");
            List<WebElement> blocks = flexWrapper.findElements(By.className("block"));
            System.out.println("Found " + blocks.size() + " blocks");

            if (!blocks.isEmpty()) {
                WebElement firstBlock = blocks.get(0);
                WebElement moviesLink = wait.until(ExpectedConditions.elementToBeClickable(
                        firstBlock.findElement(By.xpath(".//a[@title='Movies']"))
                ));
                moviesLink.click();
                System.out.println("Clicked Movies link");

                Thread.sleep(3000);

                // Scrape subnav__items
                WebElement subnavItems = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("subnav__items")));
                System.out.println("Found subnav__items");

                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(".//li/a")));

                Set<String> subnavData = new LinkedHashSet<>();
                List<WebElement> subnavElements = subnavItems.findElements(By.xpath(".//li"));
                System.out.println("Found " + subnavElements.size() + " subnav elements");

                System.out.println("Raw text of each <li> element:");
                for (int i = 0; i < subnavElements.size(); i++) {
                    WebElement element = subnavElements.get(i);
                    String text = element.getText().trim();
                    System.out.println("Element " + (i + 1) + ": '" + text + "' (Displayed: " + element.isDisplayed() + ")");
                }

                for (WebElement element : subnavElements) {
                    String text = element.getText().trim();
                    if (element.isDisplayed() && text != null && !text.isEmpty() && !text.matches("\\s+") && text.length() > 2) {
                        subnavData.add(text);
                    } else {
                        System.out.println("Skipped element: '" + text + "' (Displayed: " + element.isDisplayed() + ")");
                    }
                }

                System.out.println("Categories in subnavData: " + subnavData);

                List<String> categories = new ArrayList<>(subnavData);

                // Navigate to the International page directly
                String regionalAvailability = "";
                String[] internationalUrls = {
                        "https://www.paramountplus.com/global/",
                        "https://www.paramountplus.com/ca/global/"
                };

                boolean navigated = false;
                for (String url : internationalUrls) {
                    try {
                        driver.get(url);
                        System.out.println("Navigated to URL: " + url);
                        Thread.sleep(10000); // Wait for the page to load

                        // Scroll to ensure all content is loaded
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                        Thread.sleep(5000);

                        String pageSource = driver.getPageSource();
                        if (pageSource.contains("cbs-show-content")) {
                            System.out.println("Page contains 'cbs-show-content'");
                            navigated = true;
                            break;
                        } else {
                            System.out.println("Page does NOT contain 'cbs-show-content'");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to navigate to " + url + ": " + e.getMessage());
                    }
                }

                if (!navigated) {
                    System.out.println("None of the international URLs worked");
                    regionalAvailability = "Not available";
                } else {
                    // Wait for the cbs-show-content element to be visible
                    try {
                        WebElement cbsShowContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("cbs-show-content")
                        ));
                        System.out.println("Found cbs-show-content");

                        // Scrape all <li> tags under cbs-show-content
                        List<WebElement> liElements = cbsShowContent.findElements(By.xpath(".//li"));
                        System.out.println("Found " + liElements.size() + " <li> elements");

                        if (liElements.isEmpty()) {
                            System.out.println("No <li> elements found under cbs-show-content, defaulting to 'Not available'");
                            regionalAvailability = "Not available";
                        } else {
                            // Extract the text from each <li> (which is inside the <a> tag)
                            regionalAvailability = liElements.stream()
                                    .map(element -> element.findElement(By.tagName("a")).getText().trim())
                                    .filter(text -> !text.isEmpty())
                                    .collect(Collectors.joining(", "));
                            System.out.println("Scraped regional availability (countries only): " + regionalAvailability);
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to locate id='cbs-show-content': " + e.getMessage());
                        regionalAvailability = "Not available";

                        // Save page source for debugging
                        try (FileWriter writer = new FileWriter("page_source.html")) {
                            writer.write(driver.getPageSource());
                            System.out.println("Saved page source to page_source.html for debugging");
                        } catch (IOException ex) {
                            System.out.println("Failed to save page source: " + ex.getMessage());
                        }
                    }
                }

                // Combine plans and categories into the final data list with regional availability
                List<String[]> data = new ArrayList<>();
                int numPlans = plansData.size();
                int numCategories = categories.size();

                for (int i = 0; i < numPlans; i++) {
                    String[] plan = plansData.get(i);
                    String category = (i < numCategories) ? categories.get(i) : "";
                    data.add(new String[]{plan[0], plan[1], category, plan[2], plan[3], plan[4], plan[5], regionalAvailability});
                }

                for (int i = numPlans; i < numCategories; i++) {
                    data.add(new String[]{"", "", categories.get(i), "", "", "", "", ""});
                }

                System.out.println("Final data before writing to CSV:");
                for (String[] row : data) {
                    System.out.println("[" + row[0] + ", " + row[1] + ", " + row[2] + ", " + row[3] + ", " + row[4] + ", " + row[5] + ", " + row[6] + ", " + row[7] + "]");
                }

                // Write to CSV
                try (FileWriter csvWriter = new FileWriter("paramount_plus_data.csv")) {
                    csvWriter.append("Name,Monthly Plans,Genre,Quality,Devices,Ad,Download,Regional Availability\n");
                    for (String[] row : data) {
                        csvWriter.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                                escapeCsv(row[0]), escapeCsv(row[1]), escapeCsv(row[2]),
                                escapeCsv(row[3]), escapeCsv(row[4]), escapeCsv(row[5]),
                                escapeCsv(row[6]), escapeCsv(row[7])));
                    }
                    System.out.println("Data written to paramount_plus_data.csv");
                } catch (IOException e) {
                    System.out.println("Error writing to CSV: " + e.getMessage());
                }
            } else {
                System.out.println("No blocks found under flexWrapper");
            }

        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
            System.out.println("🚀 Browser closed.");
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void main(String[] args) {
        scrape(); // Call the scrape method from main for standalone execution
    }
}