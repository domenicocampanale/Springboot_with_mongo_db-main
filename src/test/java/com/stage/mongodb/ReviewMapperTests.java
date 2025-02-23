package com.stage.mongodb;

import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.mapper.MovieMapper;
import com.stage.mongodb.mapper.ReviewMapper;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.model.Review;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReviewMapperTests {

    private ReviewMapper reviewMapper;
    private EasyRandom easyRandom;
    private MovieMapper movieMapper;


    @BeforeAll
    void setUp() {
        reviewMapper = new ReviewMapper(movieMapper);
        easyRandom = new EasyRandom();
    }

    @Test
    void testToReviewDto() {

        Review review = easyRandom.nextObject(Review.class);
        review.setInsertDate(Instant.now());
        review.setUpdateDate(Instant.now());
        ReviewDto reviewDto = reviewMapper.toReviewDto(review);
        assertThat(reviewDto).isNotNull();
        assertThat(reviewDto.getInsertDate()).isNotNull();
        assertThat(reviewDto.getUpdateDate()).isNotNull();
        assertThat(reviewDto.getComment()).isNotNull();
        assertThat(review.getRating()).isEqualTo(reviewDto.getRating());
        assertThat(review.getComment()).isEqualTo(reviewDto.getComment());
    }


    @Test
    void testToReviewDtoWithMovieDto() {

        Review review = easyRandom.nextObject(Review.class);
        review.setInsertDate(Instant.now());
        review.setUpdateDate(Instant.now());
        Movie movie = easyRandom.nextObject(Movie.class);
        ReviewDto reviewDto = reviewMapper.toReviewDto(review, movie);
        assertThat(reviewDto).isNotNull();
        assertThat(reviewDto.getId()).isNotNull();
        assertThat(reviewDto.getInsertDate()).isNotNull();
        assertThat(reviewDto.getUpdateDate()).isNotNull();
        assertThat(reviewDto.getComment()).isNotNull();
        assertThat(reviewDto.getMovieDto()).isNotNull();

        assertThat(review.getRating()).isEqualTo(reviewDto.getRating());
        assertThat(review.getComment()).isEqualTo(reviewDto.getComment());
    }

    @Test
    void testToReviewFromDtoInput() {
        ReviewDtoInput reviewDtoInput = easyRandom.nextObject(ReviewDtoInput.class);
        Review review = reviewMapper.toReviewFromDtoInput(reviewDtoInput);
        assertThat(review).isNotNull();
        assertThat(review.getMovieId()).isNotNull();
        assertThat(review.getComment()).isNotNull();
        assertThat(reviewDtoInput.getMovieId()).isEqualTo(review.getMovieId());
        assertThat(reviewDtoInput.getRating()).isEqualTo(review.getRating());
        assertThat(reviewDtoInput.getComment()).isEqualTo(review.getComment());

    }

    @Test
    void testUpdateReviewFromDtoInput() {

        Review existingReview = easyRandom.nextObject(Review.class);
        ReviewDtoInput reviewDtoInput = easyRandom.nextObject(ReviewDtoInput.class);
        reviewMapper.updateReviewFromDtoInput(reviewDtoInput, existingReview);
        assertThat(existingReview.getId()).isNotNull();
        assertThat(existingReview.getMovieId()).isNotNull();
        assertThat(existingReview.getComment()).isNotNull();
        assertThat(existingReview.getInsertDate()).isNotNull();
        assertThat(existingReview.getUpdateDate()).isNotNull();
        assertThat(reviewDtoInput.getRating()).isEqualTo(existingReview.getRating());
        assertThat(reviewDtoInput.getComment()).isEqualTo(existingReview.getComment());
    }


    @Test
    void testUpdateReviewFromDtoUpdate() {

        Review existingReview = easyRandom.nextObject(Review.class);
        ReviewDtoUpdate reviewDtoUpdate = easyRandom.nextObject(ReviewDtoUpdate.class);

        reviewMapper.updateReviewFromDtoUpdate(reviewDtoUpdate, existingReview);

        assertThat(existingReview.getId()).isNotNull();
        assertThat(existingReview.getMovieId()).isNotNull();
        assertThat(existingReview.getComment()).isNotNull();
        assertThat(existingReview.getInsertDate()).isNotNull();
        assertThat(existingReview.getUpdateDate()).isNotNull();

        assertThat(reviewDtoUpdate.getRating()).isEqualTo(existingReview.getRating());
        assertThat(reviewDtoUpdate.getComment()).isEqualTo(existingReview.getComment());
    }


    @Test
    void testUpdateReviewFromDtoNullUpdate() {

        Review existingReview = easyRandom.nextObject(Review.class);
        reviewMapper.updateReviewFromDtoUpdate(null, existingReview);
        assertThat(existingReview.getId()).isNotNull();
        assertThat(existingReview.getMovieId()).isNotNull();
        assertThat(existingReview.getComment()).isNotNull();
        assertThat(existingReview.getInsertDate()).isNotNull();
        assertThat(existingReview.getUpdateDate()).isNotNull();

    }


    @Test
    void testUpdateReviewFromPatchDto() {

        Review existingReview = easyRandom.nextObject(Review.class);
        ReviewPatchDto reviewPatchDto = easyRandom.nextObject(ReviewPatchDto.class);
        reviewPatchDto.setComment(null);
        reviewMapper.updateReviewFromPatchDto(reviewPatchDto, existingReview);
        assertThat(existingReview.getId()).isNotNull();
        assertThat(existingReview.getMovieId()).isNotNull();
        assertThat(existingReview.getComment()).isNotNull();
        assertThat(existingReview.getInsertDate()).isNotNull();
        assertThat(existingReview.getUpdateDate()).isNotNull();

    }

    @Test
    void testUpdateReviewFromNullPatchDto() {

        Review existingReview = easyRandom.nextObject(Review.class);
        reviewMapper.updateReviewFromPatchDto(null, existingReview);
        assertThat(existingReview.getId()).isNotNull();
        assertThat(existingReview.getMovieId()).isNotNull();
        assertThat(existingReview.getComment()).isNotNull();
        assertThat(existingReview.getInsertDate()).isNotNull();
        assertThat(existingReview.getUpdateDate()).isNotNull();

    }

    @Test
    void testToReviewFromDtoInput_NullInput() {
        Review review = reviewMapper.toReviewFromDtoInput(null);
        assertThat(review).isNull();
    }

    @Test
    void testUpdateReviewFromDtoInput_NullInput() {
        Review existingReview = easyRandom.nextObject(Review.class);
        reviewMapper.updateReviewFromDtoInput(null, existingReview);
        assertThat(existingReview).isNotNull();
    }


    @Test
    void testListOfReviewsDto() {

        List<Movie> movies = easyRandom.objects(Movie.class, 2).toList();
        List<Review> reviews = easyRandom.objects(Review.class, 2).toList();
        List<ReviewDto> reviewsDto = reviewMapper.listOfReviewsDto(reviews, movies);
        assertThat(reviewsDto).isNotNull();
        assertThat(reviewsDto.size()).isEqualTo(2);
    }


    @Test
    void testformatData() {
        Instant data = Instant.now();
        String stringData = reviewMapper.formatData(data);
        assertThat(stringData).isNotNull();
    }


}
