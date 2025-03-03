package com.stage.mongodb.E2E;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MovieE2ETest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MovieRepository movieRepository;

    private MovieDtoInput input;
    private MovieDtoInput updatedInput;
    private MoviePatchDto patchDto;

    @BeforeAll
    void setup() {

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
        ResponseEntity<MovieDto> response = restTemplate.postForEntity("/api/movie", input, MovieDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        List<Movie> movies = movieRepository.findAll();
        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getTitle()).isEqualTo("New Movie");
    }

    @Test
    @Order(2)
    public void testGetMovies() {
        ResponseEntity<List<MovieDto>> response = restTemplate.exchange(
                "/api/movie/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();

        List<Movie> movies = movieRepository.findAll();
        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getTitle()).isEqualTo("New Movie");
    }

    @Test
    @Order(3)
    public void testGetMovieById() {
        String movieId = movieRepository.findAll().get(0).getId();
        ResponseEntity<MovieDto> response = restTemplate.getForEntity("/api/movie?id=" + movieId, MovieDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(movieId);
    }

    @Test
    @Order(4)
    public void testUpdateMovie() {
        String movieId = movieRepository.findAll().get(0).getId();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MovieDtoInput> requestEntity = new HttpEntity<>(updatedInput, headers);

        ResponseEntity<MovieDto> response = restTemplate.exchange("/api/movie?id=" + movieId, HttpMethod.PUT, requestEntity, MovieDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Movie");
        assertThat(response.getBody().getReleaseDate()).isEqualTo("2025-01-01");
    }

    @Test
    @Order(5)
    public void testUpdateMoviePartial() {
        String movieId = movieRepository.findAll().get(0).getId();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MoviePatchDto> requestEntity = new HttpEntity<>(patchDto, headers);

        ResponseEntity<MovieDto> response = restTemplate.exchange("/api/movie?id=" + movieId, HttpMethod.PATCH, requestEntity, MovieDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Partial Updated Movie");
    }

    @Test
    @Order(6)
    public void testDeleteMovie() {
        String movieId = movieRepository.findAll().get(0).getId();
        restTemplate.delete("/api/movie?id=" + movieId);
        assertThat(movieRepository.findById(movieId)).isEmpty();
    }
}
