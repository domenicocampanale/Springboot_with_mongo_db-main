package com.stage.mongodb.mapper;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.model.Movie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieMapperTests {

    private MovieMapper movieMapper;
    private EasyRandom easyRandom;

    @BeforeAll
    void setUp() {
        movieMapper = new MovieMapper();
        easyRandom = new EasyRandom();
    }

    @Test
    void testToMovieDto() {
        Movie movie = easyRandom.nextObject(Movie.class);
        MovieDto movieDto = movieMapper.toMovieDto(movie);
        assertThat(movieDto).isNotNull();
        assertThat(movie.getTitle()).isEqualTo(movieDto.getTitle());
        assertThat(movie.getReleaseDate()).isEqualTo(movieDto.getReleaseDate());
    }

    @Test
    void testToMovieFromDtoInput() {
        MovieDtoInput movieDtoInput = easyRandom.nextObject(MovieDtoInput.class);
        Movie movie = movieMapper.toMovieFromDtoInput(movieDtoInput);
        assertThat(movie).isNotNull();
        assertThat(movieDtoInput.getTitle()).isEqualTo(movie.getTitle());
        assertThat(movieDtoInput.getReleaseDate()).isEqualTo(movie.getReleaseDate());
    }

    @Test
    void testUpdateMovieFromDtoInput() {
        Movie existingMovie = easyRandom.nextObject(Movie.class);
        MovieDtoInput movieDtoInput = easyRandom.nextObject(MovieDtoInput.class);
        movieMapper.updateMovieFromDtoInput(movieDtoInput, existingMovie);
        assertThat(movieDtoInput.getTitle()).isEqualTo(existingMovie.getTitle());
    }

    @Test
    void testUpdateMovieFromPatchDto() {
        Movie existingMovie = easyRandom.nextObject(Movie.class);
        String originalTitle = existingMovie.getTitle();
        MoviePatchDto patchDto = easyRandom.nextObject(MoviePatchDto.class);
        patchDto.setTitle(null);
        movieMapper.updateMovieFromPatchDto(patchDto, existingMovie);
        assertThat(originalTitle).isEqualTo(existingMovie.getTitle());
    }


    @Test
    void testFormatData() {
        Instant data = Instant.now();
        String stringData = movieMapper.formatData(data);
        assertThat(stringData).matches("\\d{4}-\\d{2}-\\d{2}.*");
    }


}
