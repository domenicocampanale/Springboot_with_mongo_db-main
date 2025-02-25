package com.stage.mongodb.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.mongodb.controller.MovieController;
import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.exceptions.MovieNotFoundException;
import com.stage.mongodb.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
public class MovieControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    private MovieDto movie;
    private MovieDtoInput input;
    private MoviePatchDto patchDto;

    @BeforeEach
    void setup() {

        objectMapper = new ObjectMapper();

        movie = MovieDto.builder()
                .id("123")
                .title("Test Movie")
                .releaseDate("2024-12-22")
                .insertDate("2024-12-24")
                .updateDate("2024-12-24")
                .build();

        input = MovieDtoInput.builder()
                .title("New Movie")
                .releaseDate("2024-12-25")
                .build();

        patchDto = MoviePatchDto.builder()
                .title("Patched Movie")
                .build();
    }

    @Test
    void testGetMovies() throws Exception {

        List<MovieDto> movies = new ArrayList<>();

        movies.add(movie);

        when(movieService.getMovies()).thenReturn(movies);

        mockMvc.perform(get("/api/movie/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());

        verify(movieService).getMovies();
    }

    @Test
    void testGetMovieById() throws Exception {

        when(movieService.getMovieById("123")).thenReturn(movie);

        mockMvc.perform(get("/api/movie").param("id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));

        verify(movieService).getMovieById("123");
    }

    @Test
    void testInsertMovie() throws Exception {

        when(movieService.insertMovie(input)).thenReturn(movie);

        String jsonInput = objectMapper.writeValueAsString(input);

        mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value("123"));

        verify(movieService).insertMovie(input);
    }

    @Test
    void testUpdateMovie() throws Exception {

        when(movieService.updateMovie(input, "123")).thenReturn(movie);
        String jsonInput = objectMapper.writeValueAsString(input);

        mockMvc.perform(put("/api/movie")
                        .param("id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));

        verify(movieService).updateMovie(input, "123");
    }

    @Test
    void testUpdateMoviePartial() throws Exception {

        when(movieService.updateMoviePartial("123", patchDto)).thenReturn(movie);
        String jsonInput = objectMapper.writeValueAsString(patchDto);

        mockMvc.perform(patch("/api/movie")
                        .param("id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));

        verify(movieService).updateMoviePartial("123", patchDto);
    }

    @Test
    void testDeleteMovie() throws Exception {

        mockMvc.perform(delete("/api/movie").param("id", "123"))
                .andExpect(status().isOk());

        verify(movieService).deleteMovie("123");
    }


    @Test
    void testGetMovieById_NotFound() throws Exception {
        String movieId = "999";

        when(movieService.getMovieById(movieId)).thenThrow(new MovieNotFoundException("Movie not found"));

        mockMvc.perform(get("/api/movie").param("id", movieId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found"));

        verify(movieService).getMovieById("999");
    }

    @Test
    void testUpdateMovie_NotFound() throws Exception {
        String movieId = "999";

        doThrow(new MovieNotFoundException("Movie not found"))
                .when(movieService).updateMovie(input, movieId);

        String jsonInput = objectMapper.writeValueAsString(input);

        mockMvc.perform(put("/api/movie")
                        .param("id", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found"));

        verify(movieService).updateMovie(input, "999");

    }

    @Test
    void testUpdateMoviePartial_NotFound() throws Exception {
        String movieId = "999";

        doThrow(new MovieNotFoundException("Movie not found"))
                .when(movieService).updateMoviePartial(movieId, patchDto);

        String jsonInput = objectMapper.writeValueAsString(patchDto);

        mockMvc.perform(patch("/api/movie")
                        .param("id", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found"));

        verify(movieService).updateMoviePartial("999", patchDto);
    }

    @Test
    void testDeleteMovie_NotFound() throws Exception {
        String movieId = "999";

        doThrow(new MovieNotFoundException("Movie not found"))
                .when(movieService).deleteMovie(movieId);

        mockMvc.perform(delete("/api/movie").param("id", movieId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found"));

        verify(movieService).deleteMovie("999");
    }

    @Test
    void testMethodArgumentNotValidException() throws Exception {

        MovieDtoInput invalidInput = MovieDtoInput.builder()
                .title("")
                .releaseDate("2024-12-25")
                .build();

        String jsonInput = objectMapper.writeValueAsString(invalidInput);

        mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

    }

    @Test
    void testGenericException() throws Exception {
        when(movieService.getMovies()).thenThrow(new RuntimeException("Generic Exception"));

        mockMvc.perform(get("/api/movie/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Generic Exception"));
    }

    @Test
    void testIllegalArgumentException() throws Exception {
        when(movieService.getMovieById("invalid-id"))
                .thenThrow(new IllegalArgumentException());

        mockMvc.perform(get("/api/movie").param("id", "invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ONE OR MORE FIELDS ARE ILLEGAL ARGUMENTS"));
    }


}
