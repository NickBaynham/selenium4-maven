package org.testautomation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import java.time.Duration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

public class WaitTest {
    static final Logger log = getLogger(lookup().lookupClass());
    private WebDriver driver;

    @BeforeEach
    void setup() {
        log.info("Launching the Chrome Browser (Headless).");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        log.info("Terminating the Chrome Browser (Headless.");
        driver.quit();
    }

    @Test
    void testImplicitWait() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/loading-images.html");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement landscape = driver.findElement(By.id("landscape"));
        assertThat(landscape.getAttribute("src"))
                .containsIgnoringCase("landscape");
    }

    @Test
    void testExplicitWait() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/loading-images.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement landscape = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.id("landscape")));
        assertThat(landscape.getAttribute("src"))
                .containsIgnoringCase("landscape");
    }

    @Test
    void testSlowCalculator() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/slow-calculator.html");

        // 1 + 3
        driver.findElement(By.xpath("//span[text()='1']")).click();
        driver.findElement(By.xpath("//span[text()='+']")).click();
        driver.findElement(By.xpath("//span[text()='3']")).click();
        driver.findElement(By.xpath("//span[text()='=']")).click();

        // ... should be 4, wait for it
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBe(By.className("screen"), "4"));
    }

    @Test
    void testFluentWait() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/loading-images.html");
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class);

        WebElement landscape = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.id("landscape")));
        assertThat(landscape.getAttribute("src"))
                .containsIgnoringCase("landscape");
    }
}
