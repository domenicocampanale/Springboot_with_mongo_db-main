package com.stage.mongodb.service;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.exceptions.MovieNotFoundException;
import com.stage.mongodb.mapper.MovieMapper;
import com.stage.mongodb.model.Movie;
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
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MovieMapper movieMapper;

    public List<MovieDto> getMovies() {

        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            log.info("No movies found");
            return List.of();
        }
        log.info("Movie list correctly viewed");
        return movies.stream().map(movieMapper::toMovieDto).collect(Collectors.toList());
    }

    public MovieDto getMovieById(String id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + id + " does not exist");
            log.error(errorMessage);
            return new MovieNotFoundException(errorMessage);
        });
        log.info("Movie with ID {} correctly found", id);
        return movieMapper.toMovieDto(movie);
    }

    public MovieDto insertMovie(MovieDtoInput movieDtoInput) {

        log.debug("Attempting to insert movie: {}", movieDtoInput);
        Movie movie = movieMapper.toMovieFromDtoInput(movieDtoInput);
        movie.setInsertDate(Instant.now());
        movie.setUpdateDate(Instant.now());
        movieRepository.save(movie);
        log.info("Movie with ID {} correctly inserted", movie.getId());
        return movieMapper.toMovieDto(movie);
    }

    public MovieDto updateMovie(MovieDtoInput movieDtoInput, String id) {
        Movie existingMovie = movieRepository.findById(id).orElseThrow(() -> {
            String errorMessage = ("Movie with ID " + id + " not found for the update");
            log.error(errorMessage);
            return new MovieNotFoundException(errorMessage);
        });

        movieMapper.updateMovieFromDtoInput(movieDtoInput, existingMovie);

        existingMovie.setUpdateDate(Instant.now());

        movieRepository.save(existingMovie);

        log.info("Movie with ID {} correctly updated", id);

        return movieMapper.toMovieDto(existingMovie);
    }

    public MovieDto updateMoviePartial(String id, MoviePatchDto moviePatchDto) {

        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = ("Movie with ID " + id + " not found for the update");
                    log.error(errorMessage);
                    return new MovieNotFoundException("Movie not found");
                });

        movieMapper.updateMovieFromPatchDto(moviePatchDto, existingMovie);

        existingMovie.setUpdateDate(Instant.now());

        movieRepository.save(existingMovie);

        return movieMapper.toMovieDto(existingMovie);
    }

    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            String errorMessage = "Movie with ID " + id + " not found for the deletion";
            log.error(errorMessage);
            throw new MovieNotFoundException(errorMessage);
        }

        movieRepository.deleteById(id);
        reviewRepository.deleteByMovieId(id);

        log.info("Movie with ID {} correctly deleted with its reviews", id);

    }

}
