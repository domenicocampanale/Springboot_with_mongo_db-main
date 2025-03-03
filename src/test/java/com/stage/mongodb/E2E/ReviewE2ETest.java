package com.stage.mongodb.E2E;

import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewE2ETest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private TestRestTemplate restTemplate;

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
        ResponseEntity<ReviewDto> response = restTemplate.postForEntity("/api/review", input, ReviewDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotEmpty();
        reviewId = response.getBody().getId();
        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    public void testGetReviews() {
        ResponseEntity<List<ReviewDto>> response = restTemplate.exchange(
                "/api/review/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @Order(3)
    public void testGetReviewById() {
        ResponseEntity<ReviewDto> response = restTemplate.getForEntity("/api/review?id=" + reviewId, ReviewDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(reviewId);
    }

    @Test
    @Order(4)
    public void testUpdateReview() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReviewDtoUpdate> request = new HttpEntity<>(update, headers);

        ResponseEntity<ReviewDto> response = restTemplate.exchange(
                "/api/review?id=" + reviewId,
                HttpMethod.PUT,
                request,
                ReviewDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(3);
        assertThat(response.getBody().getComment()).isEqualTo("Updated review");
    }

    @Test
    @Order(5)
    public void testUpdateReviewPartial() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReviewPatchDto> request = new HttpEntity<>(patch, headers);

        ResponseEntity<ReviewDto> response = restTemplate.exchange(
                "/api/review?id=" + reviewId,
                HttpMethod.PATCH,
                request,
                ReviewDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getComment()).isEqualTo("Partially updated review");
    }

    @Test
    @Order(6)
    public void testDeleteReview() {
        String reviewId = reviewRepository.findAll().get(0).getId();
        restTemplate.delete("/api/review?id=" + reviewId);
        assertThat(reviewRepository.findById(reviewId)).isEmpty();
    }
}
