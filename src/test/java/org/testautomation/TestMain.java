package org.testautomation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class TestMain {
    static final Logger log = getLogger(lookup().lookupClass());
    private WebDriver driver;

    @BeforeEach
    void setup() {
        log.info("Launching the Chrome Browser.");
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        log.info("Terminating the Chrome Browser.");
        driver.quit();
    }

    @Test
    void test() {
        log.info("Executing Demo Test");
        String URL = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(URL);
        String title = driver.getTitle();
        log.info("The title of {} is {}", URL, title);

        // Verifying
        assertThat(title).isEqualTo("Hands-On Selenium WebDriver with Java");
    }
}