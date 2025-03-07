package com.stage.mongodb.PlayWright_E2E;

import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import com.microsoft.playwright.*;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class PlayWrightMovieTest {

    @Autowired
    private MovieRepository movieRepository;

    private String movieId;

    @BeforeAll
    void setup() {
        movieRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testHomePage(Page page) {
        page.navigate("http://localhost:8080/view/movie/home");
        assertEquals("Movie Home Page", page.title());
    }

    @Test
    @Order(2)
    void testAddMovie(Page page) {
        page.navigate("http://localhost:8080/view/movie/add");
        page.click("button");
        assertTrue(page.url().contains("list?success"));
    }

    @Test
    @Order(3)
    void testMovieList(Page page) {
        page.navigate("http://localhost:8080/view/movie/list");
        movieId = page.locator("li p:has-text('Movie ID:')").first().textContent().split(": ")[1];
        assertNotNull(movieId);
    }

    @Test
    @Order(4)
    void testEditMovie(Page page) {
        page.navigate("http://localhost:8080/view/movie/edit?id=" + movieId);
        page.fill("#title", "Film Modificato Playwright");
        page.click("button");
        assertTrue(page.url().contains("list?success"));
    }

    @Test
    @Order(5)
    void testMovieFound(Page page) {
        page.navigate("http://localhost:8080/view/movie/details?id=" + movieId);
        assertTrue(page.locator("//p[contains(text(), 'Title:')]").isVisible());
        assertTrue(page.locator("//p[contains(text(), 'Release Date:')]").isVisible());
    }

    @Test
    @Order(6)
    void testMovieNotFound(Page page) {
        page.navigate("http://localhost:8080/view/movie/details?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(7)
    void testEditMovieNotFound(Page page) {
        page.navigate("http://localhost:8080/view/movie/edit?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(8)
    void testDeleteMovieNotFound(Page page) {
        page.navigate("http://localhost:8080/view/movie/delete?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(9)
    void testAddMovieClientValidation(Page page) {
        page.navigate("http://localhost:8080/view/movie/add");
        page.fill("#title", "");
        page.click("button");
        assertEquals("http://localhost:8080/view/movie/add", page.url());
    }

    @Test
    @Order(10)
    void testAddMovieWithInvalidData(Page page) {
        page.navigate("http://localhost:8080/view/movie/add");
        page.fill("#title", "Test");
        page.fill("#releaseDate", "invalid-date");
        page.click("button");
        assertTrue(page.locator("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]").isVisible());
    }

    @Test
    @Order(11)
    void testEditMovieWithInvalidData(Page page) {
        page.navigate("http://localhost:8080/view/movie/edit?id=" + movieId);
        page.fill("#title", "Test");
        page.fill("#releaseDate", "invalid-date");
        page.click("button");
        assertTrue(page.locator("//div[contains(@class, 'error') and preceding-sibling::label[@for='releaseDate']]").isVisible());
    }

    @Test
    @Order(12)
    void testDeleteMovie(Page page) {
        page.navigate("http://localhost:8080/view/movie/delete?id=" + movieId);
        assertTrue(page.url().contains("list?success"));
    }
}
