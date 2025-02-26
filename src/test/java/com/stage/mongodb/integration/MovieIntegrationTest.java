package com.stage.mongodb.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.model.Movie;
import com.stage.mongodb.repository.MovieRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MovieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    private MovieDtoInput input;
    private MovieDtoInput updatedInput;
    private MoviePatchDto patchDto;

    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() {
        objectMapper = new ObjectMapper();

        input = MovieDtoInput.builder()
                .title("New Movie")
                .releaseDate("2024-12-25")
                .build();
        updatedInput = MovieDtoInput.builder()
                .title("Updated Movie")
                .releaseDate("2025-01-01")
                .build();
        patchDto = MoviePatchDto.builder()
                .title("Partial Updated Movie")
                .build();
    }

    @Test
    @Order(1)
    public void testAddMovie() throws Exception {
        movieRepository.deleteAll();
        String jsonInput = objectMapper.writeValueAsString(input);
        mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        List<Movie> movies = movieRepository.findAll();
        assertThat(movieRepository.count()).isEqualTo(1);
        assertThat(movies.get(0).getTitle()).isEqualTo("New Movie");
    }

    @Test
    @Order(2)
    public void testGetMovies() throws Exception {
        mockMvc.perform(get("/api/movie/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
        List<Movie> movies = movieRepository.findAll();
        assertThat(movieRepository.count()).isEqualTo(1);
        assertThat(movies.get(0).getTitle()).isEqualTo("New Movie");
    }

    @Test
    @Order(3)
    public void testGetMovieById() throws Exception {
        String movieId = movieRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/movie").param("id", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movieId));
    }

    @Test
    @Order(4)
    public void testUpdateMovie() throws Exception {
        String movieId = movieRepository.findAll().get(0).getId();
        String jsonInput = objectMapper.writeValueAsString(updatedInput);

        mockMvc.perform(put("/api/movie")
                        .param("id", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movieId))
                .andExpect(jsonPath("$.title").value("Updated Movie"))
                .andExpect(jsonPath("$.releaseDate").value("2025-01-01"));

        Movie updatedMovie = movieRepository.findById(movieId).orElse(null);
        assertThat(updatedMovie).isNotNull();
        assertThat(updatedMovie.getTitle()).isEqualTo("Updated Movie");
        assertThat(updatedMovie.getReleaseDate()).isEqualTo("2025-01-01");
    }

    @Test
    @Order(5)
    public void testUpdateMoviePartial() throws Exception {
        String movieId = movieRepository.findAll().get(0).getId();
        String jsonInput = objectMapper.writeValueAsString(patchDto);

        mockMvc.perform(patch("/api/movie")
                        .param("id", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movieId))
                .andExpect(jsonPath("$.title").value("Partial Updated Movie"));

        Movie updatedMovie = movieRepository.findById(movieId).orElse(null);
        assertThat(updatedMovie).isNotNull();
        assertThat(updatedMovie.getTitle()).isEqualTo("Partial Updated Movie");
    }

    @Test
    @Order(6)
    public void testDeleteMovie() throws Exception {
        String movieId = movieRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/movie").param("id", movieId))
                .andExpect(status().isOk());

        assertThat(movieRepository.findById(movieId).isEmpty()).isTrue();
    }
}