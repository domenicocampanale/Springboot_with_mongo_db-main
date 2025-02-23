package com.stage.mongodb.mapper;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.dto.MoviePatchDto;
import com.stage.mongodb.model.Movie;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class MovieMapper {


    public MovieDto toMovieDto(Movie movie) {
        if (movie == null) {
            return null;
        }

        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .insertDate(formatData(movie.getInsertDate()))
                .updateDate(formatData(movie.getUpdateDate()))
                .build();
    }


    public Movie toMovieFromDtoInput(MovieDtoInput movieDtoInput) {
        if (movieDtoInput == null) {
            return null;
        }

        return Movie.builder()
                .title(movieDtoInput.getTitle())
                .releaseDate(movieDtoInput.getReleaseDate())
                .build();
    }

    public void updateMovieFromDtoInput(MovieDtoInput movieDtoInput, Movie movie) {
        if (movieDtoInput == null) {
            return;
        }

        movie.setTitle(movieDtoInput.getTitle());
        movie.setReleaseDate(movieDtoInput.getReleaseDate());
    }


    public void updateMovieFromPatchDto(MoviePatchDto patchDto, Movie movie) {
        if (patchDto == null) {
            return;
        }
        Optional.ofNullable(patchDto.getTitle())
                .ifPresent(movie::setTitle);

        Optional.ofNullable(patchDto.getReleaseDate())
                .ifPresent(date -> movie.setReleaseDate(formatData(date)));

    }


    public String formatData(Instant data) {
        if (data == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return data.atZone(java.time.ZoneId.systemDefault()).format(formatter);
    }
}
