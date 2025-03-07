package com.stage.mongodb.integration;

import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class MovieIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MovieRepository movieRepository;

    private String baseUrl;
    private MovieDtoInput input;
    private MovieDtoInput updatedInput;
    private MoviePatchDto patchDto;
    private String movieId;

    @BeforeAll
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port + "/api/movie";

        movieRepository.deleteAll();

        input = MovieDtoInput.builder()
                .title("New Movie")
                .releaseDate("2024-12-25")
                .build();

        updatedInput = MovieDtoInput.builder()
                .title("Updated Movie")
                .releaseDate("2025-01-01")
                .build();

        patchDto = MoviePatchDto.builder()
                .title("Partial Updated Movie")
                .build();
    }

    @Test
    @Order(1)
    public void testAddMovie() {
        movieId = given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post(baseUrl)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("New Movie"))
                .extract().path("id");

        assertThat(movieId).isNotNull();
        assertThat(movieRepository.findById(movieId)).isPresent();
    }

    @Test
    @Order(2)
    public void testGetMovies() {
        given()
                .when()
                .get(baseUrl + "/all")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThan(0))
                .body("[0].title", equalTo("New Movie"));

        List<Movie> movies = movieRepository.findAll();
        assertThat(movies).hasSize(1);
    }

    @Test
    @Order(3)
    public void testGetMovieById() {
        given()
                .queryParam("id", movieId)
                .when()
                .get(baseUrl)
                .then()
                .statusCode(200)
                .body("id", equalTo(movieId))
                .body("title", equalTo("New Movie"));
    }

    @Test
    @Order(4)
    public void testUpdateMovie() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("id", movieId)
                .body(updatedInput)
                .when()
                .put(baseUrl)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Movie"))
                .body("releaseDate", equalTo("2025-01-01"));

        Movie updatedMovie = movieRepository.findById(movieId).orElseThrow();
        assertThat(updatedMovie.getTitle()).isEqualTo("Updated Movie");
    }

    @Test
    @Order(5)
    public void testUpdateMoviePartial() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("id", movieId)
                .body(patchDto)
                .when()
                .patch(baseUrl)
                .then()
                .statusCode(200)
                .body("title", equalTo("Partial Updated Movie"));

        Movie patchedMovie = movieRepository.findById(movieId).orElseThrow();
        assertThat(patchedMovie.getTitle()).isEqualTo("Partial Updated Movie");
    }

    @Test
    @Order(6)
    public void testDeleteMovie() {
        given()
                .queryParam("id", movieId)
                .when()
                .delete(baseUrl)
                .then()
                .statusCode(200);

        assertThat(movieRepository.findById(movieId)).isEmpty();
    }
}
