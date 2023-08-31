package org.testautomation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class BrowserAgnosticTest {
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
    void testScrollBy() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/long-page.html");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script = "window.scrollBy(0, 1000);";
        js.executeScript(script);
    }

    @Test
    void testScrollIntoView() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/long-page.html");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement lastElememt = driver
                .findElement(By.cssSelector("p:last-child"));
        String script = "arguments[0].scrollIntoView();";
        js.executeScript(script, lastElememt);
    }

    @Test
    void testInfiniteScroll() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/infinite-scroll.html");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        By pLocator = By.tagName("p");
        List<WebElement> paragraphs = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(pLocator, 0));
        int initParagraphsNumber = paragraphs.size();

        WebElement lastParagraph = driver.findElement(
                By.xpath(String.format("//p[%d]", initParagraphsNumber)));
        String script = "arguments[0].scrollIntoView();";
        js.executeScript(script, lastParagraph);

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(pLocator,
                initParagraphsNumber));
    }

    @Test
    void testColorPicker() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement colorPicker = driver.findElement(By.name("my-colors"));
        String initColor = colorPicker.getAttribute("value");
        log.info("The initial color is {}", initColor);

        Color red = new Color(255, 0, 0, 1);
        String script = String.format(
                "arguments[0].setAttribute('value', '%s');", red.asHex());
        js.executeScript(script, colorPicker);

        String finalColor = colorPicker.getAttribute("value");
        log.info("The final color is {}", finalColor);
        assertThat(finalColor).isNotEqualTo(initColor);
        assertThat(Color.fromString(finalColor)).isEqualTo(red);
    }

    @Test
    void testPinnedScripts() {
        String initPage = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(initPage);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        ScriptKey linkKey = js
                .pin("return document.getElementsByTagName('a')[2];");
        ScriptKey firstArgKey = js.pin("return arguments[0];");

        Set<ScriptKey> pinnedScripts = js.getPinnedScripts();
        assertThat(pinnedScripts).hasSize(2);

        WebElement formLink = (WebElement) js.executeScript(linkKey);
        formLink.click();
        assertThat(driver.getCurrentUrl()).isNotEqualTo(initPage);

        String message = "Hello world!";
        String executeScript = (String) js.executeScript(firstArgKey, message);
        assertThat(executeScript).isEqualTo(message);

        js.unpin(linkKey);
        assertThat(js.getPinnedScripts()).hasSize(1);
    }

    @Test
    void testAsyncScript() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Duration pause = Duration.ofSeconds(2);
        String script = "const callback = arguments[arguments.length - 1];"
                + "window.setTimeout(callback, " + pause.toMillis() + ");";

        long initMillis = System.currentTimeMillis();
        js.executeAsyncScript(script);
        Duration elapsed = Duration
                .ofMillis(System.currentTimeMillis() - initMillis);
        log.info("The script took {} ms to be executed", elapsed.toMillis());
        assertThat(elapsed).isGreaterThanOrEqualTo(pause);
    }

    @Test
    void testPageLoadTimeout() {
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(1));

        assertThatThrownBy(() -> driver
                .get("https://bonigarcia.dev/selenium-webdriver-java/"))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void testScriptTimeout() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(3));

        assertThatThrownBy(() -> {
            long waitMillis = Duration.ofSeconds(5).toMillis();
            String script = "const callback = arguments[arguments.length - 1];"
                    + "window.setTimeout(callback, " + waitMillis + ");";
            js.executeAsyncScript(script);
        }).isInstanceOf(ScriptTimeoutException.class);
    }

    @Test
    void testScreenshotPng() throws IOException {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        TakesScreenshot ts = (TakesScreenshot) driver;

        File screenshot = ts.getScreenshotAs(OutputType.FILE);
        log.info("Screenshot created on {}", screenshot);

        Path destination = Paths.get("screenshot.png");
        Files.move(screenshot.toPath(), destination, REPLACE_EXISTING);
        log.info("Screenshot moved to {}", destination);

        assertThat(destination).exists();
    }

    @Test
    void testScreenshotBase64() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        TakesScreenshot ts = (TakesScreenshot) driver;

        String screenshot = ts.getScreenshotAs(OutputType.BASE64);
        log.info("Screenshot in base64 "
                + "(you can copy and paste it into a browser navigation bar to watch it)\n"
                + "data:image/png;base64,{}", screenshot);
        assertThat(screenshot).isNotEmpty();
    }

    @Test
    void testWebElementScreenshot() throws IOException {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement form = driver.findElement(By.tagName("form"));
        File screenshot = form.getScreenshotAs(OutputType.FILE);
        Path destination = Paths.get("webelement-screenshot.png");
        Files.move(screenshot.toPath(), destination, REPLACE_EXISTING);

        assertThat(destination).exists();
    }

    @Test
    void testWindow() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        WebDriver.Window window = driver.manage().window();

        Point initialPosition = window.getPosition();
        Dimension initialSize = window.getSize();
        log.debug("Initial window: position {} -- size {}", initialPosition,
                initialSize);

        window.maximize();

        Point maximizedPosition = window.getPosition();
        Dimension maximizedSize = window.getSize();
        log.debug("Maximized window: position {} -- size {}", maximizedPosition,
                maximizedSize);

        assertThat(initialPosition).isNotEqualTo(maximizedPosition);
        assertThat(initialSize).isNotEqualTo(maximizedSize);
    }

    @Test
    void testHistory() {
        String baseUrl = "https://bonigarcia.dev/selenium-webdriver-java/";
        String firstPage = baseUrl + "navigation1.html";
        String secondPage = baseUrl + "navigation2.html";
        String thirdPage = baseUrl + "navigation3.html";

        driver.get(firstPage);

        driver.navigate().to(secondPage);
        driver.navigate().to(thirdPage);
        driver.navigate().back();
        driver.navigate().forward();
        driver.navigate().refresh();

        assertThat(driver.getCurrentUrl()).isEqualTo(thirdPage);
    }

    @Test
    void testShadowDom() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/shadow-dom.html");

        WebElement content = driver.findElement(By.id("content"));
        SearchContext shadowRoot = content.getShadowRoot();
        WebElement textElement = shadowRoot.findElement(By.cssSelector("p"));
        assertThat(textElement.getText()).contains("Hello Shadow DOM");
    }

    @Test
    void testReadCookies() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/cookies.html");

        WebDriver.Options options = driver.manage();
        Set<Cookie> cookies = options.getCookies();
        assertThat(cookies).hasSize(2);

        Cookie username = options.getCookieNamed("username");
        assertThat(username.getValue()).isEqualTo("John Doe");
        assertThat(username.getPath()).isEqualTo("/");

        driver.findElement(By.id("refresh-cookies")).click();
    }

    @Test
    void testAddCookies() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/cookies.html");

        WebDriver.Options options = driver.manage();
        Cookie newCookie = new Cookie("new-cookie-key", "new-cookie-value");
        options.addCookie(newCookie);
        String readValue = options.getCookieNamed(newCookie.getName())
                .getValue();
        assertThat(newCookie.getValue()).isEqualTo(readValue);

        driver.findElement(By.id("refresh-cookies")).click();
    }

    @Test
    void testEditCookie() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/cookies.html");

        WebDriver.Options options = driver.manage();
        Cookie username = options.getCookieNamed("username");
        Cookie editedCookie = new Cookie(username.getName(), "new-value");
        options.addCookie(editedCookie);

        Cookie readCookie = options.getCookieNamed(username.getName());
        assertThat(editedCookie).isEqualTo(readCookie);

        driver.findElement(By.id("refresh-cookies")).click();
    }

    @Test
    void testDeleteCookies() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/cookies.html");

        WebDriver.Options options = driver.manage();
        Set<Cookie> cookies = options.getCookies();
        Cookie username = options.getCookieNamed("username");
        options.deleteCookie(username);

        assertThat(options.getCookies()).hasSize(cookies.size() - 1);

        driver.findElement(By.id("refresh-cookies")).click();
    }

    @Test
    void test() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        Select select = new Select(driver.findElement(By.name("my-select")));
        String optionLabel = "Three";
        select.selectByVisibleText(optionLabel);

        assertThat(select.getFirstSelectedOption().getText())
                .isEqualTo(optionLabel);
    }

    @Test
    void testDatalist() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement datalist = driver.findElement(By.name("my-datalist"));
        datalist.click();

        WebElement option = driver
                .findElement(By.xpath("//datalist/option[2]"));
        String optionValue = option.getAttribute("value");
        datalist.sendKeys(optionValue);

        assertThat(optionValue).isEqualTo("New York");
    }

    @Test
    void testNewTab() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        String initHandle = driver.getWindowHandle();

        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");
        assertThat(driver.getWindowHandles().size()).isEqualTo(2);

        driver.switchTo().window(initHandle);
        driver.close();
        assertThat(driver.getWindowHandles().size()).isEqualTo(1);
    }

    @Test
    void testNewWindow() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        String initHandle = driver.getWindowHandle();

        driver.switchTo().newWindow(WindowType.WINDOW);
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");
        assertThat(driver.getWindowHandles().size()).isEqualTo(2);

        driver.switchTo().window(initHandle);
        driver.close();
        assertThat(driver.getWindowHandles().size()).isEqualTo(1);
    }

    @Test
    void testIFrames() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/iframes.html");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions
                .frameToBeAvailableAndSwitchToIt("my-iframe"));

        By pName = By.tagName("p");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(pName, 0));
        List<WebElement> paragraphs = driver.findElements(pName);
        assertThat(paragraphs).hasSize(20);
    }

    @Test
    void testFrames() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/frames.html");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String frameName = "frame-body";
        wait.until(ExpectedConditions
                .presenceOfElementLocated(By.name(frameName)));
        driver.switchTo().frame(frameName);

        By pName = By.tagName("p");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(pName, 0));
        List<WebElement> paragraphs = driver.findElements(pName);
        assertThat(paragraphs).hasSize(20);
    }

    @Test
    void testAlert() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/dialog-boxes.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.findElement(By.id("my-alert")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        assertThat(alert.getText()).isEqualTo("Hello world!");
        alert.accept();
    }

    @Test
    void testConfirm() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/dialog-boxes.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.findElement(By.id("my-confirm")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        Alert confirm = driver.switchTo().alert();
        assertThat(confirm.getText()).isEqualTo("Is this correct?");
        confirm.dismiss();
    }

    @Test
    void testPrompt() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/dialog-boxes.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.findElement(By.id("my-prompt")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        Alert prompt = driver.switchTo().alert();
        prompt.sendKeys("John Doe");
        assertThat(prompt.getText()).isEqualTo("Please enter your name");
        prompt.accept();
    }

    @Test
    void testModal() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/dialog-boxes.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.findElement(By.id("my-modal")).click();
        WebElement close = driver
                .findElement(By.xpath("//button[text() = 'Close']"));
        assertThat(close.getTagName()).isEqualTo("button");
        wait.until(ExpectedConditions.elementToBeClickable(close));
        close.click();
    }

    @Test
    void testWebStorage() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-storage.html");
        WebStorage webStorage = (WebStorage) driver;

        LocalStorage localStorage = webStorage.getLocalStorage();
        log.debug("Local storage elements: {}", localStorage.size());

        SessionStorage sessionStorage = webStorage.getSessionStorage();
        sessionStorage.keySet()
                .forEach(key -> log.debug("Session storage: {}={}", key,
                        sessionStorage.getItem(key)));
        assertThat(sessionStorage.size()).isEqualTo(2);

        sessionStorage.setItem("new element", "new value");
        assertThat(sessionStorage.size()).isEqualTo(3);

        driver.findElement(By.id("display-session")).click();
    }


}
