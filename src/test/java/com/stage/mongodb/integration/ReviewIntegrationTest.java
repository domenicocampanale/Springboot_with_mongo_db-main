package com.stage.mongodb.integration;

import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class ReviewIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private MovieRepository movieRepository;

    private ReviewDtoInput input;
    private ReviewDtoUpdate update;
    private ReviewPatchDto patch;
    private String reviewId;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        reviewRepository.deleteAll();
        movieRepository.deleteAll();

        Movie movie = movieRepository.save(
                Movie.builder()
                        .id("123")
                        .title("Test Movie")
                        .releaseDate("2024-01-01")
                        .insertDate(Instant.now())
                        .updateDate(Instant.now())
                        .build()
        );

        input = ReviewDtoInput.builder()
                .rating(5)
                .comment("Nice!")
                .movieId(movie.getId())
                .build();

        update = ReviewDtoUpdate.builder()
                .rating(3)
                .comment("Updated review")
                .build();

        patch = ReviewPatchDto.builder()
                .comment("Partially updated review")
                .build();
    }

    @Test
    @Order(1)
    public void testAddReview() {
        reviewId = given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/api/review")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("rating", equalTo(5))
                .body("comment", equalTo("Nice!"))
                .extract().path("id");

        assertThat(reviewId).isNotNull();
        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    public void testGetReviews() {
        given()
                .when()
                .get("/api/review/all")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(0))
                .body("[0].rating", equalTo(5))
                .body("[0].comment", equalTo("Nice!"));
    }

    @Test
    @Order(3)
    public void testGetReviewById() {
        given()
                .queryParam("id", reviewId)
                .when()
                .get("/api/review")
                .then()
                .statusCode(200)
                .body("id", equalTo(reviewId))
                .body("rating", equalTo(5))
                .body("comment", equalTo("Nice!"));
    }

    @Test
    @Order(4)
    public void testUpdateReview() {
        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/api/review?id=" + reviewId)
                .then()
                .statusCode(200)
                .body("rating", equalTo(3))
                .body("comment", equalTo("Updated review"));
    }

    @Test
    @Order(5)
    public void testUpdateReviewPartial() {
        given()
                .contentType(ContentType.JSON)
                .body(patch)
                .when()
                .patch("/api/review?id=" + reviewId)
                .then()
                .statusCode(200)
                .body("comment", equalTo("Partially updated review"));
    }

    @Test
    @Order(6)
    public void testDeleteReview() {
        given()
                .queryParam("id", reviewId)
                .when()
                .delete("/api/review")
                .then()
                .statusCode(200);

        assertThat(reviewRepository.findById(reviewId)).isEmpty();
    }
}
