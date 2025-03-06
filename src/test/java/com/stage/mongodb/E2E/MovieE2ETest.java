package com.stage.mongodb.E2E;

import com.stage.mongodb.repository.MovieRepository;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class MovieE2ETest {

    @Autowired
    private MovieRepository movieRepository;

    private WebDriver driver;
    private String movieId;


    @BeforeAll
    void setup() {
        movieRepository.deleteAll();
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
        driver.get("http://localhost:8080/view/movie/home");
        String title = driver.getTitle();
        assertEquals("Movie Home Page", title);
    }

    @Test
    @Order(2)
    void testAddMovie() {
        driver.get("http://localhost:8080/view/movie/add");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("list?success"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test
    @Order(3)
    void testMovieList() {
        driver.get("http://localhost:8080/view/movie/list");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement movieList = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("ul")));
        assertTrue(movieList.isDisplayed(), "Movie list is not available.");
        WebElement movieListItem = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("li")));
        assertTrue(movieListItem.isDisplayed(), "Movie list item is not available.");
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        String movieIdText = movieIdElement.getText();
        movieId = movieIdText.split(": ")[1];
    }

    @Test
    @Order(4)
    void testEditMovie() {
        driver.get("http://localhost:8080/view/movie/edit?id=" + movieId);
        WebElement titleField = driver.findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys("Film Modificato Selenium");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test
    @Order(5)
    void testMovieFound() {
        driver.get("http://localhost:8080/view/movie/details?id=" + movieId);
        WebElement titleElement = driver.findElement(By.xpath("//p[contains(text(), 'Title:')]"));
        WebElement releaseDateElement = driver.findElement(By.xpath("//p[contains(text(), 'Release Date:')]"));
        assertTrue(titleElement.isDisplayed());
        assertTrue(releaseDateElement.isDisplayed());
    }

    @Test
    @Order(6)
    void testMovieNotFound() {
        driver.get("http://localhost:8080/view/movie/details?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(7)
    void testEditMovieNotFound() {
        driver.get("http://localhost:8080/view/movie/edit?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(8)
    void testDeleteMovieNotFound() {
        driver.get("http://localhost:8080/view/movie/delete?id=999");
        WebElement errorMessage = driver.findElement(By.className("error"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(9)
    void testAddMovieClientValidation() {
        driver.get("http://localhost:8080/view/movie/add");
        WebElement titleField = driver.findElement(By.id("title"));
        titleField.clear(); // Lascia il campo vuoto
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        String currentUrl = driver.getCurrentUrl();
        assertEquals("http://localhost:8080/view/movie/add", currentUrl);
    }

    @Test
    @Order(10)
    void testAddMovieWithInvalidData() {
        driver.get("http://localhost:8080/view/movie/add");
        WebElement titleField = driver.findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys("Test");
        WebElement releaseDateField = driver.findElement(By.id("releaseDate"));
        releaseDateField.clear();
        releaseDateField.sendKeys("invalid-date");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement errorDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")));
        assertTrue(errorDate.isDisplayed());
    }

    @Test
    @Order(11)
    void testEditMovieWithInvalidData() {
        driver.get("http://localhost:8080/view/movie/list");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement movieListItem = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("li")));
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        String movieIdText = movieIdElement.getText();
        String movieId = movieIdText.split(": ")[1];
        driver.get("http://localhost:8080/view/movie/edit?id=" + movieId);
        WebElement titleField = driver.findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys("Test");
        WebElement releaseDateField = driver.findElement(By.id("releaseDate"));
        releaseDateField.clear();
        releaseDateField.sendKeys("invalid-date");
        WebElement submitButton = driver.findElement(By.tagName("button"));
        submitButton.click();
        WebElement errorDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")));
        assertTrue(errorDate.isDisplayed());
    }

    @Test
    @Order(12)
    void testDeleteMovie() {
        driver.get("http://localhost:8080/view/movie/delete?id=" + movieId);
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }
}