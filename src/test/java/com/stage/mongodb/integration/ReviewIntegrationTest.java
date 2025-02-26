package com.stage.mongodb.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.model.Review;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieRepository movieRepository;

    private ObjectMapper objectMapper;

    private ReviewDtoInput input;
    private ReviewDtoUpdate update;
    private ReviewPatchDto patch;

    @BeforeAll
    void setup() {
        objectMapper = new ObjectMapper();

        reviewRepository.deleteAll();

        Movie movie = Movie.builder()
                .id("123")
                .title("Test Movie")
                .releaseDate("2024-01-01")
                .insertDate(Instant.now())
                .updateDate(Instant.now())
                .build();

        movieRepository.deleteAll();
        movie = movieRepository.save(movie);

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
    public void testAddReview() throws Exception {

        String jsonInput = objectMapper.writeValueAsString(input);
        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviewRepository.count()).isEqualTo(1);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
    }

    @Test
    @Order(2)
    public void testGetReviews() throws Exception {
        mockMvc.perform(get("/api/review/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @Order(3)
    public void testGetReviewById() throws Exception {
        String reviewId = reviewRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/review").param("id", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    @Order(4)
    public void testUpdateReview() throws Exception {
        String reviewId = reviewRepository.findAll().get(0).getId();
        String jsonInput = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/review")
                        .param("id", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.rating").value(3))
                .andExpect(jsonPath("$.comment").value("Updated review"));

        Review updatedReview = reviewRepository.findById(reviewId).orElse(null);
        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getRating()).isEqualTo(3);
        assertThat(updatedReview.getComment()).isEqualTo("Updated review");
    }

    @Test
    @Order(5)
    public void testUpdateReviewPartial() throws Exception {
        String reviewId = reviewRepository.findAll().get(0).getId();
        String jsonInput = objectMapper.writeValueAsString(patch);

        mockMvc.perform(patch("/api/review")
                        .param("id", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.comment").value("Partially updated review"));

        Review updatedReview = reviewRepository.findById(reviewId).orElse(null);
        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getComment()).isEqualTo("Partially updated review");
    }

    @Test
    @Order(6)
    public void testDeleteReview() throws Exception {
        String reviewId = reviewRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/review").param("id", reviewId))
                .andExpect(status().isOk());

        assertThat(reviewRepository.findById(reviewId).isEmpty()).isTrue();
    }
}