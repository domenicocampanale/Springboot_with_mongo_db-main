package com.stage.mongodb.controller;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.service.MovieService;
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

class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    @Captor
    private ArgumentCaptor<String> idCaptor;

    @Captor
    private ArgumentCaptor<MovieDtoInput> movieDtoInputCaptor;

    @Captor
    private ArgumentCaptor<MoviePatchDto> moviePatchDtoCaptor;

    private EasyRandom easyRandom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();
    }

    @Test
    void testGetMovies() {
        List<MovieDto> movies = easyRandom.objects(MovieDto.class, 3).toList();
        when(movieService.getMovies()).thenReturn(movies);
        ResponseEntity<List<MovieDto>> response = movieController.getMovies();
        assertThat(response.getBody()).isEqualTo(movies);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(movieService).getMovies();
    }

    @Test
    void testGetMovieById() {
        MovieDto movie = easyRandom.nextObject(MovieDto.class);
        when(movieService.getMovieById("123")).thenReturn(movie);
        ResponseEntity<MovieDto> response = movieController.getMovieById("123");
        assertThat(response.getBody()).isEqualTo(movie);
        verify(movieService).getMovieById(idCaptor.capture());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }

    @Test
    void testInsertMovie() {
        MovieDtoInput input = easyRandom.nextObject(MovieDtoInput.class);
        MovieDto movie = easyRandom.nextObject(MovieDto.class);
        when(movieService.insertMovie(input)).thenReturn(movie);
        ResponseEntity<MovieDto> response = movieController.insert(input);
        assertThat(response.getBody()).isEqualTo(movie);
        verify(movieService).insertMovie(movieDtoInputCaptor.capture());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(movieDtoInputCaptor.getValue()).isEqualTo(input);
    }

    @Test
    void testUpdateMovie() {
        MovieDtoInput dtoInput = easyRandom.nextObject(MovieDtoInput.class);
        MovieDto updatedMovie = easyRandom.nextObject(MovieDto.class);
        when(movieService.updateMovie(dtoInput, "123")).thenReturn(updatedMovie);
        ResponseEntity<MovieDto> response = movieController.update("123", dtoInput);
        assertThat(response.getBody()).isEqualTo(updatedMovie);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(movieService).updateMovie(movieDtoInputCaptor.capture(), idCaptor.capture());
        assertThat(movieDtoInputCaptor.getValue()).isEqualTo(dtoInput);
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }

    @Test
    void testUpdateMoviePartial() {
        MoviePatchDto patchDto = easyRandom.nextObject(MoviePatchDto.class);
        MovieDto updatedMovie = easyRandom.nextObject(MovieDto.class);
        when(movieService.updateMoviePartial("123", patchDto)).thenReturn(updatedMovie);
        ResponseEntity<MovieDto> response = movieController.updateMoviePartial("123", patchDto);
        assertThat(response.getBody()).isEqualTo(updatedMovie);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(movieService).updateMoviePartial(idCaptor.capture(), moviePatchDtoCaptor.capture());
        assertThat(moviePatchDtoCaptor.getValue()).isEqualTo(patchDto);
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }

    @Test
    void testDeleteMovie() {
        movieController.delete("123");
        verify(movieService).deleteMovie(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo("123");
    }
}
