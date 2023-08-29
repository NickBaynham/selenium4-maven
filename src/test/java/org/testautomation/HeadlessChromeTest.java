package org.testautomation;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class HeadlessChromeTest {
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
    void test() {
        log.info("Executing Demo Test");
        String URL = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(URL);
        log.info("URL: {}", driver.getCurrentUrl());
        log.info("TITLE: {}", driver.getTitle());
        String title = driver.getTitle();
        log.info("The title of {} is {}", URL, title);

        // Verifying
        assertThat(title).isEqualTo("Hands-On Selenium WebDriver with Java");
    }

    @Test
    void testBasicMethods() {
        String sutUrl = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(sutUrl);

        assertThat(driver.getTitle())
                .isEqualTo("Hands-On Selenium WebDriver with Java");
        assertThat(driver.getCurrentUrl()).isEqualTo(sutUrl);
        assertThat(driver.getPageSource()).containsIgnoringCase("</html>");
    }

    @Test
    void testSessionId() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");

        SessionId sessionId = ((RemoteWebDriver) driver).getSessionId();
        assertThat(sessionId).isNotNull();
        log.info("The sessionId is {}", sessionId.toString());
    }

    @Test
    void testByTagName() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement textarea = driver.findElement(By.tagName("textarea"));
        assertThat(textarea.getDomAttribute("rows")).isEqualTo("3");
    }

    @Test
    void testByHtmlAttributes() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        // By name
        WebElement textByName = driver.findElement(By.name("my-text"));
        assertThat(textByName.isEnabled()).isTrue();

        // By id
        WebElement textById = driver.findElement(By.id("my-text-id"));
        assertThat(textById.getAttribute("type")).isEqualTo("text");
        assertThat(textById.getDomAttribute("type")).isEqualTo("text");
        assertThat(textById.getDomProperty("type")).isEqualTo("text");

        assertThat(textById.getAttribute("myprop")).isEqualTo("myvalue");
        assertThat(textById.getDomAttribute("myprop")).isEqualTo("myvalue");
        assertThat(textById.getDomProperty("myprop")).isNull();

        // By class name
        List<WebElement> byClassName = driver
                .findElements(By.className("form-control"));
        assertThat(byClassName.size()).isPositive();
        assertThat(byClassName.get(0).getAttribute("name")).isEqualTo("my-text");
    }

    @Test
    void testByLinkText() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement linkByText = driver
                .findElement(By.linkText("Return to index"));
        assertThat(linkByText.getTagName()).isEqualTo("a");
        assertThat(linkByText.getCssValue("cursor")).isEqualTo("pointer");

        WebElement linkByPartialText = driver
                .findElement(By.partialLinkText("index"));
        assertThat(linkByPartialText.getLocation())
                .isEqualTo(linkByText.getLocation());
        assertThat(linkByPartialText.getRect()).isEqualTo(linkByText.getRect());
    }

    @Test
    void testByCssSelectorBasic() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement hidden = driver
                .findElement(By.cssSelector("input[type=hidden]"));
        assertThat(hidden.isDisplayed()).isFalse();
    }

    @Test
    void testByCssSelectorAdvanced() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement checkbox1 = driver
                .findElement(By.cssSelector("[type=checkbox]:checked"));
        assertThat(checkbox1.getAttribute("id")).isEqualTo("my-check-1");
        assertThat(checkbox1.isSelected()).isTrue();

        WebElement checkbox2 = driver
                .findElement(By.cssSelector("[type=checkbox]:not(:checked)"));
        assertThat(checkbox2.getAttribute("id")).isEqualTo("my-check-2");
        assertThat(checkbox2.isSelected()).isFalse();
    }

    @Test
    void testByXPathBasic() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement hidden = driver
                .findElement(By.xpath("//input[@type='hidden']"));
        assertThat(hidden.isDisplayed()).isFalse();
    }

    @Test
    void testByXPathAdvanced() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement radio1 = driver
                .findElement(By.xpath("//*[@type='radio' and @checked]"));
        assertThat(radio1.getAttribute("id")).isEqualTo("my-radio-1");
        assertThat(radio1.isSelected()).isTrue();

        WebElement radio2 = driver
                .findElement(By.xpath("//*[@type='radio' and not(@checked)]"));
        assertThat(radio2.getAttribute("id")).isEqualTo("my-radio-2");
        assertThat(radio2.isSelected()).isFalse();
    }

    @Test
    void testByIdOrName() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement fileElement = driver.findElement(new ByIdOrName("my-file"));
        assertThat(fileElement.getAttribute("id")).isBlank();
        assertThat(fileElement.getAttribute("name")).isNotBlank();
    }

    @Test
    void testByChained() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        List<WebElement> rowsInForm = driver.findElements(
                new ByChained(By.tagName("form"), By.className("row")));
        assertThat(rowsInForm.size()).isEqualTo(1);
    }

    @Test
    void testByAll() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        List<WebElement> rowsInForm = driver.findElements(
                new ByAll(By.tagName("form"), By.className("row")));
        assertThat(rowsInForm.size()).isEqualTo(5);
    }

    @Test
    void testRelativeLocators() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement link = driver.findElement(By.linkText("Return to index"));
        RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(By.tagName("input"));
        WebElement readOnly = driver.findElement(relativeBy.above(link));
        assertThat(readOnly.getAttribute("name")).isEqualTo("my-readonly");
    }

    @Test
    void testDatePicker() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        // Get the current date from the system clock
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentDay = today.getDayOfMonth();

        // Click on the date picker to open the calendar
        WebElement datePicker = driver.findElement(By.name("my-date"));
        datePicker.click();

        // Click on the current month by searching by text
        WebElement monthElement = driver.findElement(By.xpath(
                String.format("//th[contains(text(),'%d')]", currentYear)));
        monthElement.click();

        // Click on the left arrow using relative locators
        WebElement arrowLeft = driver.findElement(
                RelativeLocator.with(By.tagName("th")).toRightOf(monthElement));
        arrowLeft.click();

        // Click on the current month of that year
        WebElement monthPastYear = driver.findElement(RelativeLocator
                .with(By.cssSelector("span[class$=focused]")).below(arrowLeft));
        monthPastYear.click();

        // Click on the present day in that month
        WebElement dayElement = driver.findElement(By.xpath(String.format(
                "//td[@class='day' and contains(text(),'%d')]", currentDay)));
        dayElement.click();

        // Get the final date on the input text 7
        String oneYearBack = datePicker.getAttribute("value");
        log.debug("Final date in date picker: {}", oneYearBack);

        // Assert that the expected date is equal to the one selected in the
        // date picker  8
        LocalDate previousYear = today.minusYears(1);
        DateTimeFormatter dateFormat = DateTimeFormatter
                .ofPattern("MM/dd/yyyy");
        String expectedDate = previousYear.format(dateFormat);
        log.debug("Expected date: {}", expectedDate);

        assertThat(oneYearBack).isEqualTo(expectedDate);
    }

    @Test
    void testSendKeys() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement inputText = driver.findElement(By.name("my-text"));
        String textValue = "Hello World!";
        inputText.sendKeys(textValue);
        assertThat(inputText.getAttribute("value")).isEqualTo(textValue);

        inputText.clear();
        assertThat(inputText.getAttribute("value")).isEmpty();
    }

    @Test
    void testSlider() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");

        WebElement slider = driver.findElement(By.name("my-range"));
        String initValue = slider.getAttribute("value");
        log.debug("The initial value of the slider is {}", initValue);

        for (int i = 0; i < 5; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }

        String endValue = slider.getAttribute("value");
        log.debug("The final value of the slider is {}", endValue);
        assertThat(initValue).isNotEqualTo(endValue);
    }

    @Test
    void testNavigation() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");

        driver.findElement(By.xpath("//a[text()='Navigation']")).click();
        driver.findElement(By.xpath("//a[text()='Next']")).click();
        driver.findElement(By.xpath("//a[text()='3']")).click();
        driver.findElement(By.xpath("//a[text()='2']")).click();
        driver.findElement(By.xpath("//a[text()='Previous']")).click();

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertThat(bodyText).contains("Lorem ipsum");
    }

    @Test
    void testContextAndDoubleClick() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/dropdown-menu.html");
        Actions actions = new Actions(driver);

        WebElement dropdown2 = driver.findElement(By.id("my-dropdown-2"));
        actions.contextClick(dropdown2).build().perform();
        WebElement contextMenu2 = driver.findElement(By.id("context-menu-2"));
        assertThat(contextMenu2.isDisplayed()).isTrue();

        WebElement dropdown3 = driver.findElement(By.id("my-dropdown-3"));
        actions.doubleClick(dropdown3).build().perform();
        WebElement contextMenu3 = driver.findElement(By.id("context-menu-3"));
        assertThat(contextMenu3.isDisplayed()).isTrue();
    }

    @Test
    void testMouseOver() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/mouse-over.html");
        Actions actions = new Actions(driver);

        List<String> imageList = Arrays.asList("compass", "calendar", "award",
                "landscape");
        for (String imageName : imageList) {
            String xpath = String.format("//img[@src='img/%s.png']", imageName);
            WebElement image = driver.findElement(By.xpath(xpath));
            actions.moveToElement(image).build().perform();

            WebElement caption = driver.findElement(
                    RelativeLocator.with(By.tagName("div")).near(image));

            assertThat(caption.getText()).containsIgnoringCase(imageName);
        }
    }

    @Test
    void testDragAndDrop() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/drag-and-drop.html");
        Actions actions = new Actions(driver);

        WebElement draggable = driver.findElement(By.id("draggable"));
        int offset = 100;
        Point initLocation = draggable.getLocation();
        actions.dragAndDropBy(draggable, offset, 0)
                .dragAndDropBy(draggable, 0, offset)
                .dragAndDropBy(draggable, -offset, 0)
                .dragAndDropBy(draggable, 0, -offset).build().perform();
        assertThat(initLocation).isEqualTo(draggable.getLocation());

        WebElement target = driver.findElement(By.id("target"));
        actions.dragAndDrop(draggable, target).build().perform();
        assertThat(target.getLocation()).isEqualTo(draggable.getLocation());
    }

    @Test
    void testClickAndHold() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/draw-in-canvas.html");
        Actions actions = new Actions(driver);

        WebElement canvas = driver.findElement(By.tagName("canvas"));
        actions.moveToElement(canvas).clickAndHold();

        int numPoints = 10;
        int radius = 30;
        for (int i = 0; i <= numPoints; i++) {
            double angle = Math.toRadians((double) (360 * i) / numPoints);
            double x = Math.sin(angle) * radius;
            double y = Math.cos(angle) * radius;
            actions.moveByOffset((int) x, (int) y);
        }

        actions.release(canvas).build().perform();
    }

    @Test
    void testCopyAndPaste() {
        driver.get(
                "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");
        Actions actions = new Actions(driver);

        WebElement inputText = driver.findElement(By.name("my-text"));
        WebElement textarea = driver.findElement(By.name("my-textarea"));

        Keys modifier = SystemUtils.IS_OS_MAC ? Keys.COMMAND : Keys.CONTROL;
        actions.sendKeys(inputText, "hello world").keyDown(modifier)
                .sendKeys(inputText, "a").sendKeys(inputText, "c")
                .sendKeys(textarea, "v").build().perform();

        assertThat(inputText.getAttribute("value"))
                .isEqualTo(textarea.getAttribute("value"));
    }
}
