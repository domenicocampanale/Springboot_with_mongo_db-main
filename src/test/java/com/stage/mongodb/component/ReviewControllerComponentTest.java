package com.stage.mongodb.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.mongodb.controller.ReviewController;
import com.stage.mongodb.dto.*;
import com.stage.mongodb.exceptions.MovieNotFoundException;
import com.stage.mongodb.exceptions.ReviewNotFoundException;
import com.stage.mongodb.service.ReviewService;
import com.stage.mongodb.utils.SpacedDisplayNameGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(SpacedDisplayNameGenerator.class)
public class ReviewControllerComponentTest {


    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private ReviewDto review;
    private ReviewDtoInput input;
    private ReviewDtoUpdate update;
    private ReviewPatchDto patch;

    @BeforeAll
    void setUp(WebApplicationContext webApplicationContext) {

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MovieDto movieDto = MovieDto.builder()
                .id("m1")
                .title("Inception")
                .releaseDate("2010-07-16")
                .insertDate("2024-02-24")
                .updateDate("2024-02-24")
                .build();

        review = ReviewDto.builder()
                .id("123")
                .movieDto(movieDto)
                .rating(5)
                .comment("Great movie!")
                .insertDate("2024-02-24")
                .updateDate("2024-02-24")
                .build();

        input = ReviewDtoInput.builder()
                .rating(5)
                .comment("Nice!")
                .movieId("123")
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
    void testGetReviews() throws Exception {
        List<ReviewDto> reviews = new ArrayList<>();
        reviews.add(review);

        when(reviewService.getReviews()).thenReturn(reviews);

        mockMvc.perform(get("/api/review/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].movieDto.id").value("m1"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great movie!"));

        verify(reviewService).getReviews();
    }

    @Test
    void testGetReviewById() throws Exception {

        when(reviewService.getReviewById("123")).thenReturn(review);

        mockMvc.perform(get("/api/review").param("id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.movieDto.id").value("m1"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));

        verify(reviewService).getReviewById("123");
    }

    @Test
    void testInsertReview() throws Exception {

        when(reviewService.insertReview(input)).thenReturn(review);

        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.movieDto.id").value("m1"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));

        verify(reviewService).insertReview(input);
    }

    @Test
    void testUpdateReview() throws Exception {

        when(reviewService.updateReview(update, "123")).thenReturn(review);

        mockMvc.perform(put("/api/review").param("id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.movieDto.id").value("m1"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));

        verify(reviewService).updateReview(update, "123");
    }

    @Test
    void testUpdateReviewPartial() throws Exception {

        when(reviewService.updateReviewPartial("123", patch)).thenReturn(review);

        mockMvc.perform(patch("/api/review").param("id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.movieDto.id").value("m1"))
                .andExpect(jsonPath("$.comment").value("Great movie!"));

        verify(reviewService).updateReviewPartial("123", patch);
    }

    @Test
    void testDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/review").param("id", "123"))
                .andExpect(status().isOk());

        verify(reviewService).deleteReview("123");
    }


    @Test
    void testGetReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById("999")).thenThrow(new ReviewNotFoundException("Review with ID 999 does not exist"));

        mockMvc.perform(get("/api/review").param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review with ID 999 does not exist"));

        verify(reviewService).getReviewById("999");
    }

    @Test
    void testInsertReview_MovieNotFound() throws Exception {
        ReviewDtoInput input = ReviewDtoInput.builder()
                .rating(5)
                .comment("Nice!")
                .movieId("999")
                .build();

        when(reviewService.insertReview(input)).thenThrow(new MovieNotFoundException("Movie with ID 999 not found for the review"));

        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie with ID 999 not found for the review"));

        verify(reviewService).insertReview(input);
    }

    @Test
    void testUpdateReview_NotFound() throws Exception {
        ReviewDtoUpdate update = ReviewDtoUpdate.builder()
                .rating(3)
                .comment("Updated review")
                .build();

        when(reviewService.updateReview(update, "999")).thenThrow(new ReviewNotFoundException("Review with ID 999 not found for the update"));

        mockMvc.perform(put("/api/review").param("id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review with ID 999 not found for the update"));

        verify(reviewService).updateReview(update, "999");
    }

    @Test
    void testUpdateReviewPartial_NotFound() throws Exception {
        ReviewPatchDto patch = ReviewPatchDto.builder()
                .comment("Partially updated review")
                .build();

        when(reviewService.updateReviewPartial("999", patch)).thenThrow(new ReviewNotFoundException("Review not found"));

        mockMvc.perform(patch("/api/review").param("id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found"));

        verify(reviewService).updateReviewPartial("999", patch);
    }

    @Test
    void testDeleteReview_NotFound() throws Exception {
        doThrow(new ReviewNotFoundException("Review with ID 999 not found for the deletion"))
                .when(reviewService).deleteReview("999");

        mockMvc.perform(delete("/api/review").param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review with ID 999 not found for the deletion"));

        verify(reviewService).deleteReview("999");
    }

    @Test
    void testMethodArgumentNotValidException() throws Exception {

        ReviewDtoInput invalidInput = ReviewDtoInput.builder()
                .rating(-1)
                .comment("Nice!")
                .movieId("123")
                .build();

        String jsonInput = objectMapper.writeValueAsString(invalidInput);

        mockMvc.perform(post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGenericException() throws Exception {
        when(reviewService.getReviews()).thenThrow(new RuntimeException("Generic Exception"));

        mockMvc.perform(get("/api/review/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Generic Exception"));
    }

    @Test
    void testIllegalArgumentException() throws Exception {
        when(reviewService.getReviewById("invalid-id"))
                .thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/api/review").param("id", "invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ONE OR MORE FIELDS ARE ILLEGAL ARGUMENTS"));
    }

}
