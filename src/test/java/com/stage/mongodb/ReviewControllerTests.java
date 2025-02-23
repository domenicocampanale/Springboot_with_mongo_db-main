package com.stage.mongodb;

import com.stage.mongodb.controller.ReviewController;
import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.service.ReviewService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Captor
    private ArgumentCaptor<String> idCaptor;

    @Captor
    private ArgumentCaptor<ReviewDtoInput> reviewDtoInputCaptor;

    @Captor
    private ArgumentCaptor<ReviewDtoUpdate> reviewDtoUpdateCaptor;

    @Captor
    private ArgumentCaptor<ReviewPatchDto> reviewPatchDtoCaptor;

    private EasyRandom easyRandom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();
    }

    @Test
    void testGetReviews() {
        List<ReviewDto> reviews = easyRandom.objects(ReviewDto.class, 3).toList();
        when(reviewService.getReviews()).thenReturn(reviews);
        ResponseEntity<List<ReviewDto>> response = reviewController.getReviews();
        assertThat(response.getBody()).isEqualTo(reviews);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reviewService).getReviews();
    }

    @Test
    void testGetReviewById() {
        ReviewDto review = easyRandom.nextObject(ReviewDto.class);
        when(reviewService.getReviewById("123")).thenReturn(review);
        ResponseEntity<ReviewDto> response = reviewController.getReviewById("123");
        assertThat(response.getBody()).isEqualTo(review);
        verify(reviewService).getReviewById(idCaptor.capture());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }

    @Test
    void testInsertReview() {
        ReviewDtoInput input = easyRandom.nextObject(ReviewDtoInput.class);
        ReviewDto review = easyRandom.nextObject(ReviewDto.class);
        when(reviewService.insertReview(input)).thenReturn(review);
        ResponseEntity<ReviewDto> response = reviewController.insert(input);
        assertThat(response.getBody()).isEqualTo(review);
        verify(reviewService).insertReview(reviewDtoInputCaptor.capture());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reviewDtoInputCaptor.getValue()).isEqualTo(input);
    }

    @Test
    void testUpdateReview() {
        ReviewDtoUpdate update = easyRandom.nextObject(ReviewDtoUpdate.class);
        ReviewDto updatedReview = easyRandom.nextObject(ReviewDto.class);
        when(reviewService.updateReview(update, "123")).thenReturn(updatedReview);
        ResponseEntity<ReviewDto> response = reviewController.update("123", update);
        assertThat(response.getBody()).isEqualTo(updatedReview);
        verify(reviewService).updateReview(reviewDtoUpdateCaptor.capture(), idCaptor.capture());
        assertThat(reviewDtoUpdateCaptor.getValue()).isEqualTo(update);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }

    @Test
    void testUpdateReviewPartial() {
        ReviewPatchDto patchDto = easyRandom.nextObject(ReviewPatchDto.class);
        ReviewDto updatedReview = easyRandom.nextObject(ReviewDto.class);
        when(reviewService.updateReviewPartial("123", patchDto)).thenReturn(updatedReview);
        ResponseEntity<ReviewDto> response = reviewController.updateReviewPartial("123", patchDto);
        assertThat(response.getBody()).isEqualTo(updatedReview);
        verify(reviewService).updateReviewPartial(idCaptor.capture(), reviewPatchDtoCaptor.capture());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(idCaptor.getValue()).isEqualTo("123");
        assertThat(reviewPatchDtoCaptor.getValue()).isEqualTo(patchDto);
    }

    @Test
    void testDeleteReview() {
        reviewController.delete("123");
        verify(reviewService).deleteReview(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }
}
