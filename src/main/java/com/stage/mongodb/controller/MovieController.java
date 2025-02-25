package com.stage.mongodb.controller;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.exceptions.ErrorDetails;
import com.stage.mongodb.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movie")
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/all")
    @Operation(description = "Find all movies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of movies correctly viewed",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<List<MovieDto>> getMovies() {
        log.info("Request for showing movies list");
        List<MovieDto> moviesDto = movieService.getMovies();
        return ResponseEntity.ok(moviesDto);
    }


    @GetMapping(params = "id")
    @Operation(description = "Find movie by corresponding ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movie found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovieDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<MovieDto> getMovieById(@RequestParam String id) {
        log.info("Request for showing movie with id {} ", id);
        MovieDto movieDto = movieService.getMovieById(id);
        return ResponseEntity.status(HttpStatus.OK).body(movieDto);

    }

    @PostMapping
    @Operation(description = "insert a movie into the movie collection")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Movie correctly created",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MovieDto.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
            })

    public ResponseEntity<MovieDto> insert(@Valid @RequestBody MovieDtoInput movieDtoInput) {
        MovieDto movieDto = movieService.insertMovie(movieDtoInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(movieDto);

    }

    @PutMapping(params = "id")
    @Operation(description = "update an existing movie")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Movie correctly updated",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MovieDto.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
            })
    public ResponseEntity<MovieDto> update(@RequestParam String id, @Valid @RequestBody MovieDtoInput movieDtoInput) {
        log.info("Request for updating movie with id {}", id);
        MovieDto movieDto = movieService.updateMovie(movieDtoInput, id);
        return ResponseEntity.status(HttpStatus.OK).body(movieDto);
    }

    @PatchMapping(params = "id")
    @Operation(description = "update an existing movie by not passing all of the attributes")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Movie correctly updated",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MovieDto.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
            })
    public ResponseEntity<MovieDto> updateMoviePartial(@RequestParam String id,
                                                       @Valid @RequestBody MoviePatchDto moviePatchDto) {
        log.info("Request for partial update of movie with id {}", id);
        MovieDto movieDto = movieService.updateMoviePartial(id, moviePatchDto);
        return ResponseEntity.status(HttpStatus.OK).body(movieDto);
    }

    @DeleteMapping(params = "id")
    @Operation(description = "delete an existing movie")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movie correctly deleted"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public void delete(@RequestParam String id) {
        log.info("Request for deleting movie with id {}", id);
        movieService.deleteMovie(id);
    }
}
