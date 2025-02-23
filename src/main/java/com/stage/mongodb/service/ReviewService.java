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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final ReviewMapper reviewMapper;

    public List<ReviewDto> getReviews() {
        List<Review> reviews = reviewRepository.findAll();

        if (reviews.isEmpty()) {
            log.info("No reviews found");
            return List.of();
        }

        List<Movie> movies = movieRepository
                .findAllById(reviews.stream().map(Review::getMovieId).collect(Collectors.toSet()));

        List<ReviewDto> reviewsDto = reviewMapper.listOfReviewsDto(reviews, movies);

        log.info("Review list correctly viewed");

        return reviewsDto;
    }

    public ReviewDto getReviewById(String id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + id + " does not exist");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        Movie existingMovie = movieRepository.findById(review.getMovieId()).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + review.getMovieId() + " does not exist");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        log.info("Review with ID {} correctly found", id);
        return reviewMapper.toReviewDto(review, existingMovie);
    }

    public ReviewDto insertReview(ReviewDtoInput reviewDtoInput) {

        if (!movieRepository.existsById(reviewDtoInput.getMovieId())) {
            String errorMessage = "Movie with ID " + reviewDtoInput.getMovieId() + " not found for the review ";
            log.error(errorMessage);
            throw new MovieNotFoundException(errorMessage);
        }

        log.info("Attempting to insert review: {}", reviewDtoInput);
        Review review = reviewMapper.toReviewFromDtoInput(reviewDtoInput);

        review.setInsertDate(Instant.now());
        review.setUpdateDate(Instant.now());

        reviewRepository.save(review);
        log.info("Review with ID {} correctly inserted", review.getId());

        Movie existingMovie = movieRepository.findById(reviewDtoInput.getMovieId()).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + reviewDtoInput.getMovieId() + " does not exist");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        return reviewMapper.toReviewDto(review, existingMovie);
    }

    public ReviewDto updateReview(ReviewDtoUpdate reviewDtoUpdate, String id) {
        Review existingReview = reviewRepository.findById(id).orElseThrow(() -> {
            String errorMessage = ("Review with ID " + id + " not found for the update");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        reviewMapper.updateReviewFromDtoUpdate(reviewDtoUpdate, existingReview);

        existingReview.setUpdateDate(Instant.now());

        reviewRepository.save(existingReview);

        log.info("Review with ID {} correctly updated", id);

        Movie existingMovie = movieRepository.findById(existingReview.getMovieId()).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + existingReview.getMovieId() + " does not exist");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        return reviewMapper.toReviewDto(existingReview, existingMovie);
    }

    public ReviewDto updateReviewPartial(String id, ReviewPatchDto reviewPatchDto) {

        Review existingReview = reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        reviewMapper.updateReviewFromPatchDto(reviewPatchDto, existingReview);

        existingReview.setUpdateDate(Instant.now());

        reviewRepository.save(existingReview);

        Movie existingMovie = movieRepository.findById(existingReview.getMovieId()).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + existingReview.getMovieId() + " does not exist");
            log.error(errorMessage);
            return new ReviewNotFoundException(errorMessage);
        });

        return reviewMapper.toReviewDto(existingReview, existingMovie);
    }

    public void deleteReview(String id) {
        if (!reviewRepository.existsById(id)) {
            String errorMessage = "Review with ID " + id + " not found for the deletion" + id;
            log.error(errorMessage);
            throw new ReviewNotFoundException(errorMessage);
        }

        reviewRepository.deleteById(id);

        log.info("Review with ID {} correctly deleted", id);

    }

}
