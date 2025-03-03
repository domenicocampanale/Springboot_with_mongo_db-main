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

        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .insertDate(formatData(movie.getInsertDate()))
                .updateDate(formatData(movie.getUpdateDate()))
                .build();
    }

    public Movie toMovieFromDtoInput(MovieDtoInput movieDtoInput) {

        return Movie.builder()
                .title(movieDtoInput.getTitle())
                .releaseDate(movieDtoInput.getReleaseDate())
                .build();
    }

    public void updateMovieFromDtoInput(MovieDtoInput movieDtoInput, Movie movie) {

        movie.setTitle(movieDtoInput.getTitle());
        movie.setReleaseDate(movieDtoInput.getReleaseDate());
    }

    public void updateMovieFromPatchDto(MoviePatchDto patchDto, Movie movie) {

        Optional.ofNullable(patchDto.getTitle())
                .ifPresent(movie::setTitle);

        Optional.ofNullable(patchDto.getReleaseDate())
                .ifPresent(movie::setReleaseDate);
    }

    public String formatData(Instant data) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return data.atZone(java.time.ZoneId.systemDefault()).format(formatter);
    }
}
