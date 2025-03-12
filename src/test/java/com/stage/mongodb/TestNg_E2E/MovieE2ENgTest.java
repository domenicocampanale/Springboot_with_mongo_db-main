package com.stage.mongodb.TestNg_E2E;

import io.github.bonigarcia.wdm.WebDriverManager;
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
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SpringBootTest
public class MovieE2ENgTest {

    private WebDriver driver;
    private String movieId;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
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
        driver.get("http://localhost:8080/view/movie/" + url);
    }

    private WebElement findElement(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private void clickButton(By locator) {
        findElement(locator).click();
    }

    @Test(priority = 1)
    public void testHomePage() {
        navigateTo("home");
        assertEquals(driver.getTitle(), "Movie Home Page");
    }

    @Test(priority = 2)
    public void testAddMovie() {
        navigateTo("add");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test(priority = 3)
    public void testMovieList() {
        navigateTo("list");
        WebElement movieListItem = findElement(By.tagName("li"));
        WebElement movieIdElement = movieListItem.findElement(By.xpath(".//p[contains(text(), 'Movie ID:')]"));
        movieId = movieIdElement.getText().split(": ")[1];

        // Aggiungi un controllo per assicurarti che movieId non sia nullo
        assertTrue(movieId != null && !movieId.isEmpty(), "Movie ID non trovato!");
    }

    @Test(priority = 4)
    public void testEditMovie() {
        if (movieId == null) {
            throw new IllegalStateException("movieId non è stato inizializzato!");
        }

        navigateTo("edit?id=" + movieId);
        WebElement titleField = findElement(By.id("title"));
        titleField.clear();
        titleField.sendKeys("Film Modificato Selenium");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }

    @Test(priority = 5)
    public void testMovieFound() {
        if (movieId == null) {
            throw new IllegalStateException("movieId non è stato inizializzato!");
        }

        navigateTo("details?id=" + movieId);
        assertTrue(findElement(By.xpath("//p[contains(text(), 'Title:')]")).isDisplayed());
        assertTrue(findElement(By.xpath("//p[contains(text(), 'Release Date:')]")).isDisplayed());
    }

    @Test(priority = 6)
    public void testMovieNotFound() {
        navigateTo("details?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test(priority = 7)
    public void testEditMovieNotFound() {
        navigateTo("edit?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test(priority = 8)
    public void testDeleteMovieNotFound() {
        navigateTo("delete?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test(priority = 9)
    public void testAddMovieClientValidation() {
        navigateTo("add");
        findElement(By.id("title")).clear();
        clickButton(By.tagName("button"));
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/view/movie/add");
    }

    @Test(priority = 10)
    public void testAddMovieWithInvalidData() {
        navigateTo("add");
        findElement(By.id("title")).sendKeys("Test");
        findElement(By.id("releaseDate")).sendKeys("invalid-date");
        clickButton(By.tagName("button"));
        assertTrue(findElement(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")).isDisplayed());
    }

    @Test(priority = 11)
    public void testEditMovieWithInvalidData() {
        navigateTo("edit?id=" + movieId);
        findElement(By.id("title")).sendKeys("Test");
        findElement(By.id("releaseDate")).sendKeys("invalid-date");
        clickButton(By.tagName("button"));
        assertTrue(findElement(By.xpath("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]")).isDisplayed());
    }

    @Test(priority = 12)
    public void testPatchMovie() {
        navigateTo("patch?id=" + movieId);
        WebElement fieldToUpdate = findElement(By.id("fieldToUpdate"));
        fieldToUpdate.sendKeys("title");
        WebElement valueField = findElement(By.id("updateValue"));
        valueField.clear();
        valueField.sendKeys("Film Aggiornato con PATCH");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
        navigateTo("details?id=" + movieId);
        WebElement titleElement = findElement(By.xpath("//p[contains(text(), 'Title:')]"));
        assertTrue(titleElement.getText().contains("Film Aggiornato con PATCH"));
    }

    @Test(priority = 13)
    public void testPatchMovieClientValidation() {
        navigateTo("patch?id=" + movieId);
        WebElement titleField = findElement(By.id("updateValue"));
        titleField.clear();
        clickButton(By.tagName("button"));
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/view/movie/patch?id=" + movieId);
    }

    @Test(priority = 14)
    public void testPatchMovieNotFound() {
        navigateTo("patch?id=999");
        assertTrue(findElement(By.className("error")).isDisplayed());
    }

    @Test(priority = 15)
    public void testPatchPartialSuccess() {
        navigateTo("patch?id=" + movieId);
        WebElement fieldToUpdate = findElement(By.id("fieldToUpdate"));
        fieldToUpdate.sendKeys("title");
        WebElement valueField = findElement(By.id("updateValue"));
        valueField.clear();
        valueField.sendKeys("Film Solo Titolo Aggiornato");
        clickButton(By.tagName("button"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
        navigateTo("details?id=" + movieId);
        WebElement titleElement = findElement(By.xpath("//p[contains(text(), 'Title:')]"));
        assertTrue(titleElement.getText().contains("Film Solo Titolo Aggiornato"));
    }

    @Test(priority = 17)
    public void testDeleteMovie() {
        navigateTo("delete?id=" + movieId);
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("list?success"));
    }
}
