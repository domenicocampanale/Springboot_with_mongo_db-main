package com.stage.mongodb.PlayWright_E2E;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class PlayWrightReviewTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private String reviewId;
    private String movieId;

    @BeforeAll
    void setup() {
        movieRepository.deleteAll();
        reviewRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testHomePage(Page page) {
        page.navigate("http://localhost:8080/view/review/home");
        assertEquals("Reviews Home Page", page.title());
    }

    @Test
    @Order(2)
    void testAddReview(Page page) {
        page.navigate("http://localhost:8080/view/movie/add");
        page.click("button");
        page.waitForURL("**/list**");
        page.navigate("http://localhost:8080/view/movie/list");
        movieId = page.locator("li p:has-text('Movie ID:')").first().textContent().split(": ")[1].trim();

        page.navigate("http://localhost:8080/view/review/add");
        page.fill("#movieId", movieId);
        page.click("button");
        page.waitForURL("**/list**");
        assertTrue(page.url().contains("list"));
    }

    @Test
    @Order(3)
    void testReviewList(Page page) {
        page.navigate("http://localhost:8080/view/review/list");
        reviewId = page.locator("li p:has-text('Review ID:')").first().textContent().split(": ")[1];
        assertNotNull(reviewId);
    }

    @Test
    @Order(4)
    void testEditReview(Page page) {
        page.navigate("http://localhost:8080/view/review/edit?id=" + reviewId);
        page.fill("#comment", "Commento Modificato Playwright");
        page.click("button");
        assertTrue(page.url().contains("list?success"));
    }

    @Test
    @Order(5)
    void testReviewFound(Page page) {
        page.navigate("http://localhost:8080/view/review/details?id=" + reviewId);
        assertTrue(page.locator("p:has-text('Comment:')").isVisible());
        assertTrue(page.locator("p:has-text('Rating:')").isVisible());
    }

    @Test
    @Order(6)
    void testReviewNotFound(Page page) {
        page.navigate("http://localhost:8080/view/review/details?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(7)
    void testEditReviewNotFound(Page page) {
        page.navigate("http://localhost:8080/view/review/edit?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(8)
    void testDeleteReviewNotFound(Page page) {
        page.navigate("http://localhost:8080/view/review/delete?id=999");
        assertTrue(page.locator(".error").isVisible());
    }

    @Test
    @Order(9)
    void testAddReviewClientValidation(Page page) {
        page.navigate("http://localhost:8080/view/review/add");
        page.fill("#comment", "");
        page.click("button");
        assertEquals("http://localhost:8080/view/review/add", page.url());
    }

    @Test
    @Order(10)
    void testAddReviewWithInvalidData(Page page) {
        page.navigate("http://localhost:8080/view/movie/list");
        movieId = page.locator("li p:has-text('Movie ID:')").first().textContent().split(": ")[1].trim();

        page.navigate("http://localhost:8080/view/review/add");
        page.fill("#movieId", movieId);
        page.fill("#comment", "Test");
        page.fill("#rating", "100");
        page.click("button");
        assertTrue(page.locator("input#rating + div.error").isVisible());
    }

    @Test
    @Order(11)
    void testEditReviewWithInvalidData(Page page) {
        page.navigate("http://localhost:8080/view/review/edit?id=" + reviewId);
        page.fill("#rating", "100");
        page.click("button");
        assertTrue(page.locator("input#rating + div.error").isVisible());
    }

    @Test
    @Order(12)
    void testDeleteReview(Page page) {
        page.navigate("http://localhost:8080/view/review/delete?id=" + reviewId);
        assertTrue(page.url().contains("list?success"));
    }
}