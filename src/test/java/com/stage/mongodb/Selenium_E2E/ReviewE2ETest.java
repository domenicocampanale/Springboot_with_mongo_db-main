package com.stage.mongodb.Selenium_E2E;

import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class ReviewE2ETest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieRepository movieRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private String reviewId;
    private String movieId;

    @BeforeAll
    void setup() {
        movieRepository.deleteAll();
        reviewRepository.deleteAll();
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterAll
    void tearDown() {
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

    @Test
    @Order(1)
    void testHomePage() {
        navigateTo("/view/review/home");
        assertEquals("Reviews Home Page", driver.getTitle());
    }

    @Test
    @Order(2)
    void testAddReview() {
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

    @Test
    @Order(3)
    void testReviewList() {
        navigateTo("/view/review/list");
        WebElement reviewListItem = findElement(By.tagName("li"));
        reviewId = reviewListItem.findElement(By.xpath(".//p[contains(text(), 'Review ID:')]")).getText().split(": ")[1];
    }

    @Test
    @Order(4)
    void testEditReview() {
        navigateTo("/view/review/edit?id=" + reviewId);
        sendKeys(By.id("comment"), "Commento Modificato Selenium");
        click(By.tagName("button"));
        assertUrlContains("list?success");
    }

    @Test
    @Order(5)
    void testReviewFound() {
        navigateTo("/view/review/details?id=" + reviewId);
        assertElementDisplayed(By.xpath("//p[contains(text(), 'Comment:')]"));
        assertElementDisplayed(By.xpath("//p[contains(text(), 'Rating:')]"));
    }

    @Test
    @Order(6)
    void testReviewNotFound() {
        navigateTo("/view/review/details?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test
    @Order(7)
    void testEditReviewNotFound() {
        navigateTo("/view/review/edit?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test
    @Order(8)
    void testDeleteReviewNotFound() {
        navigateTo("/view/review/delete?id=999");
        assertElementDisplayed(By.className("error"));
    }

    @Test
    @Order(9)
    void testAddReviewClientValidation() {
        navigateTo("/view/review/add");
        sendKeys(By.id("comment"), "");
        click(By.tagName("button"));
        assertEquals("http://localhost:8080/view/review/add", driver.getCurrentUrl());
    }

    @Test
    @Order(10)
    void testAddReviewWithInvalidData() {
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

    @Test
    @Order(11)
    void testEditReviewWithInvalidData() {
        navigateTo("/view/review/edit?id=" + reviewId);
        sendKeys(By.id("rating"), "100");
        click(By.tagName("button"));
        assertElementDisplayed(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='rating']]"));
    }

    @Test
    @Order(12)
    void testDeleteReview() {
        navigateTo("/view/review/delete?id=" + reviewId);
        assertUrlContains("list?success");
    }
}