package com.stage.mongodb.TestNg_E2E;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.testng.Assert.*;

@SpringBootTest
public class ReviewE2ENgTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String reviewId;
    private String movieId;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void navigateTo(String url) {
        driver.get("http://localhost:8080" + url);
    }

    private WebElement findElement(By by) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    private List<WebElement> findElements(By by) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private void click(By by) {
        findElement(by).click();
    }

    private void sendKeys(By by, String text) {
        WebElement element = findElement(by);
        element.clear();
        element.sendKeys(text);
    }

    private void assertUrlContains(String expectedUrl) {
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains(expectedUrl));
    }

    private void assertElementDisplayed(By by) {
        assertTrue(findElement(by).isDisplayed());
    }

    private WebElement waitForElement(By by) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    @Test(priority = 1)
    public void testHomePage() {
        navigateTo("/view/review/home");
        assertEquals(driver.getTitle(), "Reviews Home Page");
    }

    @Test(priority = 2)
    public void testAddReview() {
        navigateTo("/view/movie/add");
        click(By.tagName("button"));
        wait.until(ExpectedConditions.urlContains("list"));
        assertUrlContains("list");
        navigateTo("/view/movie/list");
        List<WebElement> movieItems = findElements(By.tagName("li"));
        assertFalse(movieItems.isEmpty(), "No movies found in the list!");
        movieId = movieItems.get(0).findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]")).getText().split(": ")[1].trim();
        navigateTo("/view/review/add");
        sendKeys(By.id("movieId"), movieId);
        click(By.tagName("button"));
        assertUrlContains("list");
    }

    @Test(priority = 3)
    public void testReviewList() {
        navigateTo("/view/review/list");
        WebElement reviewListItem = waitForElement(By.tagName("li"));
        reviewId = reviewListItem.findElement(By.xpath(".//p[contains(text(), 'Review ID:')]")).getText().split(": ")[1];
    }
    @Test(priority = 4)
    public void testEditReview() {
        navigateTo("/view/review/edit?id=" + reviewId);
        sendKeys(By.id("comment"), "Commento Modificato Selenium");
        click(By.tagName("button"));
        assertUrlContains("list?success");
    }

    @Test(priority = 5)
    public void testReviewFound() {
        navigateTo("/view/review/details?id=" + reviewId);
        assertElementDisplayed(By.xpath("//p[contains(text(), 'Comment:')]"));
        assertElementDisplayed(By.xpath("//p[contains(text(), 'Rating:')]"));
    }

    @Test(priority = 6)
    public void testReviewNotFound() {
        navigateTo("/view/review/details?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test(priority = 7)
    public void testEditReviewNotFound() {
        navigateTo("/view/review/edit?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test(priority = 8)
    public void testDeleteReviewNotFound() {
        navigateTo("/view/review/delete?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test(priority = 9)
    public void testAddReviewClientValidation() {
        navigateTo("/view/review/add");
        sendKeys(By.id("comment"), "");
        click(By.tagName("button"));
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/view/review/add");
    }

    @Test(priority = 10)
    public void testAddReviewWithInvalidData() {
        navigateTo("/view/movie/list");
        List<WebElement> movieItems = findElements(By.tagName("li"));
        movieId = movieItems.get(0).findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]")).getText().split(": ")[1].trim();

        navigateTo("/view/review/add");
        sendKeys(By.id("movieId"), movieId);
        sendKeys(By.id("comment"), "Test");
        sendKeys(By.id("rating"), "100");
        click(By.tagName("button"));
        assertElementDisplayed(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='rating']]"));
    }

    @Test(priority = 11)
    public void testEditReviewWithInvalidData() {
        navigateTo("/view/review/edit?id=" + reviewId);
        sendKeys(By.id("rating"), "100");
        click(By.tagName("button"));
        assertElementDisplayed(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='rating']]"));
    }

    @Test(priority = 12)
    public void testDeleteReview() {
        navigateTo("/view/review/delete?id=" + reviewId);
        assertUrlContains("list?success");
    }
}
