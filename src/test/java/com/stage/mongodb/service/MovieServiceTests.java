package com.stage.mongodb.service;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.exceptions.MovieNotFoundException;
import com.stage.mongodb.mapper.MovieMapper;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.repository.ReviewRepository;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    @Captor
    private ArgumentCaptor<Movie> movieCaptor;

    @Captor
    private ArgumentCaptor<String> idCaptor;

    private EasyRandom easyRandom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();
    }

    @Test
    void testGetMovies() {
        List<Movie> movies = easyRandom.objects(Movie.class, 3).collect(Collectors.toList());
        List<MovieDto> movieDtos = easyRandom.objects(MovieDto.class, 3).collect(Collectors.toList());
        when(movieRepository.findAll()).thenReturn(movies);
        when(movieMapper.toMovieDto(any(Movie.class))).thenReturn(movieDtos.get(0), movieDtos.get(1), movieDtos.get(2));
        List<MovieDto> result = movieService.getMovies();
        assertThat(result).hasSize(3).isEqualTo(movieDtos);
        verify(movieRepository).findAll();
        verify(movieMapper, times(3)).toMovieDto(any(Movie.class));
    }

    @Test
    void testGetMovieById() {
        Movie movie = easyRandom.nextObject(Movie.class);
        MovieDto movieDto = easyRandom.nextObject(MovieDto.class);
        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        when(movieMapper.toMovieDto(movie)).thenReturn(movieDto);
        MovieDto result = movieService.getMovieById(movie.getId());
        assertThat(result).isEqualTo(movieDto);
        verify(movieRepository).findById(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(movie.getId());
    }

    @Test
    void testGetMovieById_NotFound() {
        String id = "non-existent-id";
        when(movieRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.getMovieById(id));
        verify(movieRepository).findById(id);
    }

    @Test
    void testInsertMovie() {
        MovieDtoInput input = easyRandom.nextObject(MovieDtoInput.class);
        Movie movie = easyRandom.nextObject(Movie.class);
        MovieDto movieDto = easyRandom.nextObject(MovieDto.class);
        when(movieMapper.toMovieFromDtoInput(input)).thenReturn(movie);
        when(movieMapper.toMovieDto(movie)).thenReturn(movieDto);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);
        MovieDto result = movieService.insertMovie(input);
        assertThat(result).isEqualTo(movieDto);
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue()).isEqualTo(movie);
    }

    @Test
    void testUpdateMovie() {
        Movie existingMovie = easyRandom.nextObject(Movie.class);
        MovieDtoInput input = easyRandom.nextObject(MovieDtoInput.class);
        MovieDto movieDto = easyRandom.nextObject(MovieDto.class);
        when(movieRepository.findById(existingMovie.getId())).thenReturn(Optional.of(existingMovie));
        doNothing().when(movieMapper).updateMovieFromDtoInput(input, existingMovie);
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.toMovieDto(existingMovie)).thenReturn(movieDto);
        MovieDto result = movieService.updateMovie(input, existingMovie.getId());
        assertThat(result).isEqualTo(movieDto);
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue()).isEqualTo(existingMovie);
    }

    @Test
    void testGetEmptyMovies() {
        when(movieRepository.findAll()).thenReturn(Collections.emptyList());
        List<MovieDto> result = movieService.getMovies();
        assertThat(result).isEmpty();
        verify(movieRepository).findAll();
        verify(movieMapper, never()).toMovieDto(any(Movie.class));
    }

    @Test
    void testUpdateMovie_NotFound() {
        String id = "invalid-id";
        MovieDtoInput input = easyRandom.nextObject(MovieDtoInput.class);
        when(movieRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.updateMovie(input, id));
        verify(movieRepository).findById(id);
    }

    @Test
    void testUpdateMoviePartial() {
        Movie existingMovie = easyRandom.nextObject(Movie.class);
        MoviePatchDto patchDto = easyRandom.nextObject(MoviePatchDto.class);
        MovieDto movieDto = easyRandom.nextObject(MovieDto.class);
        when(movieRepository.findById(existingMovie.getId())).thenReturn(Optional.of(existingMovie));
        doNothing().when(movieMapper).updateMovieFromPatchDto(patchDto, existingMovie);
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.toMovieDto(existingMovie)).thenReturn(movieDto);
        MovieDto result = movieService.updateMoviePartial(existingMovie.getId(), patchDto);
        assertThat(result).isEqualTo(movieDto);
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue()).isEqualTo(existingMovie);
    }

    @Test
    void testUpdateMoviePartial_NotFound() {
        String id = "invalid-id";
        MoviePatchDto patchDto = easyRandom.nextObject(MoviePatchDto.class);
        when(movieRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.updateMoviePartial(id, patchDto));
        verify(movieRepository).findById(id);
    }

    @Test
    void testDeleteMovie() {
        String id = "123";
        when(movieRepository.existsById(id)).thenReturn(true);
        doNothing().when(movieRepository).deleteById(id);
        doNothing().when(reviewRepository).deleteByMovieId(id);
        movieService.deleteMovie(id);
        verify(movieRepository).deleteById(idCaptor.capture());
        verify(reviewRepository).deleteByMovieId(id);
        assertThat(idCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void testDeleteMovie_NotFound() {
        String id = "non-existent-id";
        when(movieRepository.existsById(id)).thenReturn(false);
        assertThrows(MovieNotFoundException.class, () -> movieService.deleteMovie(id));
        verify(movieRepository).existsById(id);
    }
}
