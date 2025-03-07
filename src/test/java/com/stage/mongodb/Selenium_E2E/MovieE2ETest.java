package com.stage.mongodb.Selenium_E2E;

import com.stage.mongodb.repository.MovieRepository;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
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

    private void navigateTo(String url) {
        driver.get("http://localhost:8080/view/movie/" + url);
    }

    private WebElement findElement(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private void clickButton(By locator) {
        findElement(locator).click();
    }

    @Test
    @Order(1)
    void testHomePage() {
        navigateTo("home");
        assertEquals("Movie Home Page", driver.getTitle());
    }

    @Test
    @Order(2)
    void testAddMovie() {
        navigateTo("add");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test
    @Order(3)
    void testMovieList() {
        navigateTo("list");
        WebElement movieListItem = findElement(By.tagName("li"));
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        movieId = movieIdElement.getText().split(": ")[1];
    }

    @Test
    @Order(4)
    void testEditMovie() {
        navigateTo("edit?id=" + movieId);
        WebElement titleField = findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys("Film Modificato Selenium");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test
    @Order(5)
    void testMovieFound() {
        navigateTo("details?id=" + movieId);
        assertTrue(findElement(By.xpath("//p[contains(text(), 'Title:')]")).isDisplayed());
        assertTrue(findElement(By.xpath("//p[contains(text(), 'Release Date:')]")).isDisplayed());
    }

    @Test
    @Order(6)
    void testMovieNotFound() {
        navigateTo("details?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test
    @Order(7)
    void testEditMovieNotFound() {
        navigateTo("edit?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test
    @Order(8)
    void testDeleteMovieNotFound() {
        navigateTo("delete?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test
    @Order(9)
    void testAddMovieClientValidation() {
        navigateTo("add");
        findElement(By.id("title")).clear();
        clickButton(By.tagName("button"));
        assertEquals("http://localhost:8080/view/movie/add", driver.getCurrentUrl());
    }

    @Test
    @Order(10)
    void testAddMovieWithInvalidData() {
        navigateTo("add");
        findElement(By.id("title")).sendKeys("Test");
        findElement(By.id("releaseDate")).sendKeys("invalid-date");
        clickButton(By.tagName("button"));
        assertTrue(findElement(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")).isDisplayed());
    }

    @Test
    @Order(11)
    void testEditMovieWithInvalidData() {
        navigateTo("edit?id=" + movieId);
        findElement(By.id("title")).sendKeys("Test");
        findElement(By.id("releaseDate")).sendKeys("invalid-date");
        clickButton(By.tagName("button"));
        assertTrue(findElement(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")).isDisplayed());
    }

    @Test
    @Order(12)
    void testDeleteMovie() {
        navigateTo("delete?id=" + movieId);
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }
}
