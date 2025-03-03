package com.stage.mongodb.service;

import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.exceptions.MovieNotFoundException;
import com.stage.mongodb.exceptions.ReviewNotFoundException;
import com.stage.mongodb.mapper.ReviewMapper;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.model.Review;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;
import org.junit.jupiter.api.BeforeEach;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    @Captor
    private ArgumentCaptor<String> idCaptor;

    private EasyRandom easyRandom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();
    }

    @Test
    void testGetReviews() {
        List<Review> reviews = easyRandom.objects(Review.class, 3).collect(Collectors.toList());
        List<Movie> movies = easyRandom.objects(Movie.class, 3).collect(Collectors.toList());
        List<ReviewDto> reviewDtos = easyRandom.objects(ReviewDto.class, 3).collect(Collectors.toList());
        when(reviewRepository.findAll()).thenReturn(reviews);
        when(movieRepository.findAllById(anySet())).thenReturn(movies);
        when(reviewMapper.listOfReviewsDto(reviews, movies)).thenReturn(reviewDtos);
        List<ReviewDto> result = reviewService.getReviews();
        assertThat(result).hasSize(3).isEqualTo(reviewDtos);
        verify(reviewRepository).findAll();
        verify(movieRepository).findAllById(anySet());
        verify(reviewMapper).listOfReviewsDto(reviews, movies);
    }

    @Test
    void testGetReviewById() {
        Review review = easyRandom.nextObject(Review.class);
        Movie movie = easyRandom.nextObject(Movie.class);
        ReviewDto reviewDto = easyRandom.nextObject(ReviewDto.class);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(movieRepository.findById(review.getMovieId())).thenReturn(Optional.of(movie));
        when(reviewMapper.toReviewDto(review, movie)).thenReturn(reviewDto);
        ReviewDto result = reviewService.getReviewById(review.getId());
        assertThat(result).isEqualTo(reviewDto);
    }

    @Test
    void testGetReviewById_NotFound() {
        String id = "invalid-id";
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(id));
    }

    @Test
    void testGetReviewByMovieId_NotFound() {
        String id = "123";
        Review review = easyRandom.nextObject(Review.class);

        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(movieRepository.findById(review.getMovieId())).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(id));

        verify(movieRepository).findById(review.getMovieId());
    }

    @Test
    void testInsertReview() {
        ReviewDtoInput input = easyRandom.nextObject(ReviewDtoInput.class);
        Review review = easyRandom.nextObject(Review.class);
        ReviewDto reviewDto = easyRandom.nextObject(ReviewDto.class);
        Movie movie = easyRandom.nextObject(Movie.class);
        when(movieRepository.findById(input.getMovieId())).thenReturn(Optional.ofNullable(movie));
        when(reviewMapper.toReviewFromDtoInput(input)).thenReturn(review);
        when(reviewMapper.toReviewDto(review, movie)).thenReturn(reviewDto);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        ReviewDto result = reviewService.insertReview(input);
        assertThat(result).isEqualTo(reviewDto);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview).isEqualTo(review);
    }

    @Test
    void testInsertReview_MovieNotFound() {
        ReviewDtoInput input = easyRandom.nextObject(ReviewDtoInput.class);
        assertThrows(MovieNotFoundException.class, () -> reviewService.insertReview(input));
    }

    @Test
    void testUpdateReview() {
        String id = "review-id";
        ReviewDtoUpdate updateDto = easyRandom.nextObject(ReviewDtoUpdate.class);
        Review existingReview = easyRandom.nextObject(Review.class);
        Movie movie = easyRandom.nextObject(Movie.class);
        ReviewDto reviewDto = easyRandom.nextObject(ReviewDto.class);
        when(reviewRepository.findById(id)).thenReturn(Optional.of(existingReview));
        when(movieRepository.findById(existingReview.getMovieId())).thenReturn(Optional.of(movie));
        when(reviewMapper.toReviewDto(existingReview, movie)).thenReturn(reviewDto);
        ReviewDto result = reviewService.updateReview(updateDto, id);
        assertThat(result).isEqualTo(reviewDto);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview).isEqualTo(existingReview);
    }

    @Test
    void testUpdateReview_NotFound() {
        String id = "invalid-id";
        ReviewDtoUpdate updateDto = easyRandom.nextObject(ReviewDtoUpdate.class);
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReview(updateDto, id));
    }

    @Test
    void testUpdateReview_MovieNotFound() {
        String id = "review-id";
        ReviewDtoUpdate updateDto = easyRandom.nextObject(ReviewDtoUpdate.class);
        Review existingReview = easyRandom.nextObject(Review.class);

        when(reviewRepository.findById(id)).thenReturn(Optional.of(existingReview));
        when(movieRepository.findById(existingReview.getMovieId())).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReview(updateDto, id)); // Lancia eccezione
    }

    @Test
    void testUpdateReviewFromPatchDto() {
        String id = "review-id";
        ReviewPatchDto patchDto = easyRandom.nextObject(ReviewPatchDto.class);
        patchDto.setComment(null);
        Review existingReview = easyRandom.nextObject(Review.class);
        Movie existingMovie = easyRandom.nextObject(Movie.class);
        ReviewDto updatedReviewDto = easyRandom.nextObject(ReviewDto.class);
        when(reviewRepository.findById(id)).thenReturn(Optional.of(existingReview));
        when(movieRepository.findById(existingReview.getMovieId())).thenReturn(Optional.of(existingMovie));
        when(reviewRepository.save(existingReview)).thenReturn(existingReview);
        when(reviewMapper.toReviewDto(existingReview, existingMovie)).thenReturn(updatedReviewDto);
        ReviewDto result = reviewService.updateReviewPartial(id, patchDto);
        assertThat(result).isEqualTo(updatedReviewDto);
        verify(reviewRepository).findById(id);
        verify(reviewMapper).updateReviewFromPatchDto(patchDto, existingReview);
        verify(reviewRepository).save(existingReview);
        verify(movieRepository).findById(existingReview.getMovieId());
        verify(reviewMapper).toReviewDto(existingReview, existingMovie);
    }

    @Test
    void testUpdateReviewFromPatchDto_MovieNotFound() {
        String id = "review-id";
        ReviewPatchDto patchDto = easyRandom.nextObject(ReviewPatchDto.class);
        patchDto.setComment(null); // Impostiamo un campo null per il test
        Review existingReview = easyRandom.nextObject(Review.class);

        // Simula il caso in cui la recensione esista, ma il film non venga trovato
        when(reviewRepository.findById(id)).thenReturn(Optional.of(existingReview));
        when(movieRepository.findById(existingReview.getMovieId())).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReviewPartial(id, patchDto)); // Lancia eccezione
    }

    @Test
    void testDeleteReview() {
        String id = "review-id";
        when(reviewRepository.existsById(id)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(id);

        reviewService.deleteReview(id);

        verify(reviewRepository).deleteById(idCaptor.capture());
        String capturedId = idCaptor.getValue();
        assertThat(capturedId).isEqualTo(id);
    }

    @Test
    void testDeleteReview_NotFound() {
        String id = "invalid-id";
        when(reviewRepository.existsById(id)).thenReturn(false);
        assertThrows(ReviewNotFoundException.class, () -> reviewService.deleteReview(id));
    }

    @Test
    void testGetEmptyReviews() {

        when(reviewRepository.findAll()).thenReturn(Collections.emptyList());
        List<ReviewDto> result = reviewService.getReviews();
        assertThat(result).isEmpty();
        verify(reviewRepository).findAll();
        verify(reviewMapper, never()).toReviewDto(any(Review.class));
    }
}
