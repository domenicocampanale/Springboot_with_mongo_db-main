package com.stage.mongodb.E2E;

import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
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
public class ReviewE2ETest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieRepository movieRepository;
    private WebDriver driver;
    private String reviewId;
    private String movieId;

    @BeforeAll
    void setup() {
        movieRepository.deleteAll();
        reviewRepository.deleteAll();
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testHomePage() {
        driver.get("http://localhost:8080/view/review/home");
        String title = driver.getTitle();
        assertEquals("Reviews Home Page", title);
    }

    @Test
    @Order(2)
    void testAddReview() {
        driver.get("http://localhost:8080/view/movie/add");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("list"));
        String currentUrl = driver.getCurrentUrl();
        assert currentUrl != null;
        assertTrue(currentUrl.contains("list"), "Movie creation did not redirect to the expected page.");
        driver.get("http://localhost:8080/view/movie/list");
        WebElement movieList = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ul")));
        assertTrue(movieList.isDisplayed(), "Movie list is not available.");
        List<WebElement> movieItems = movieList.findElements(By.tagName("li"));
        assertFalse(movieItems.isEmpty(), "No movies found in the list!");
        WebElement movieListItem = movieItems.get(0); // Primo film nella lista
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        String movieIdText = movieIdElement.getText();
        String movieId = movieIdText.split(": ")[1].trim();
        driver.get("http://localhost:8080/view/review/add");
        WebElement movieIdField = driver.findElement(By.id("movieId"));
        movieIdField.clear();
        movieIdField.sendKeys(movieId);
        submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        assertTrue(driver.getCurrentUrl().contains("list"), "Review creation did not redirect correctly.");
    }

    @Test
    @Order(3)
    void testReviewList() {
        driver.get("http://localhost:8080/view/review/list");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement reviewList = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ul")));
        assertTrue(reviewList.isDisplayed(), "Review list is not available.");
        WebElement reviewListItem = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("li")));
        assertTrue(reviewListItem.isDisplayed(), "Review list item is not available.");
        WebElement reviewIdElement = reviewListItem.findElement(By.xpath(".//p[contains(text(), 'Review ID:')]"));
        String reviewIdText = reviewIdElement.getText();
        reviewId = reviewIdText.split(": ")[1];
    }

    @Test
    @Order(4)
    void testEditReview() {
        driver.get("http://localhost:8080/view/review/edit?id=" + reviewId);
        WebElement commentField = driver.findElement(By.id("comment"));
        commentField.clear();
        commentField.sendKeys("Commento Modificato Selenium");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test
    @Order(5)
    void testReviewFound() {
        driver.get("http://localhost:8080/view/review/details?id=" + reviewId);
        WebElement commentElement = driver.findElement(By.xpath("//p[contains(text(), 'Comment:')]"));
        WebElement ratingElement = driver.findElement(By.xpath("//p[contains(text(), 'Rating:')]"));
        assertTrue(commentElement.isDisplayed());
        assertTrue(ratingElement.isDisplayed());
    }

    @Test
    @Order(6)
    void testReviewNotFound() {
        driver.get("http://localhost:8080/view/review/details?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(7)
    void testEditReviewNotFound() {
        driver.get("http://localhost:8080/view/review/edit?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(8)
    void testDeleteReviewNotFound() {
        driver.get("http://localhost:8080/view/review/delete?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(9)
    void testAddReviewClientValidation() {
        driver.get("http://localhost:8080/view/review/add");
        WebElement commentField = driver.findElement(By.id("comment"));
        commentField.clear();
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        String currentUrl = driver.getCurrentUrl();
        assertEquals("http://localhost:8080/view/review/add", currentUrl);
    }

    @Test
    @Order(10)
    void testAddReviewWithInvalidData() {
        driver.get("http://localhost:8080/view/movie/list");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement movieList = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ul")));
        assertTrue(movieList.isDisplayed(), "Movie list is not available.");
        List<WebElement> movieItems = movieList.findElements(By.tagName("li"));
        assertFalse(movieItems.isEmpty(), "No movies found in the list!");
        WebElement movieListItem = movieItems.get(0);
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        String movieIdText = movieIdElement.getText();
        String movieId = movieIdText.split(": ")[1].trim();

        driver.get("http://localhost:8080/view/review/add");
        WebElement movieIdField = driver.findElement(By.id("movieId"));
        movieIdField.clear();
        movieIdField.sendKeys(movieId); // Usa l'ID del film appena recuperato
        WebElement commentField = driver.findElement(By.id("comment"));
        commentField.clear();
        commentField.sendKeys("Test");
        WebElement ratingField = driver.findElement(By.id("rating"));
        ratingField.clear();
        ratingField.sendKeys("100");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebElement errorRating = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='rating']]")));
        assertTrue(errorRating.isDisplayed());
    }

    @Test
    @Order(11)
    void testEditReviewWithInvalidData() {
        driver.get("http://localhost:8080/view/review/edit?id=" + reviewId);
        WebElement ratingField = driver.findElement(By.id("rating"));
        ratingField.clear();
        ratingField.sendKeys("100");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement errorRating = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='rating']]")));
        assertTrue(errorRating.isDisplayed());
    }

    @Test
    @Order(12)
    void testDeleteReview() {
        driver.get("http://localhost:8080/view/review/delete?id=" + reviewId);
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

}