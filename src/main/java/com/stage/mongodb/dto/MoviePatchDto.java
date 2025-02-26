package com.stage.mongodb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;


@EqualsAndHashCode
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MoviePatchDto {

    @Size(min = 1, max = 100, message = "The title cannot be too short or too long")
    private String title;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "The releaseDate must follow the format YYYY-MM-DD")
    private String releaseDate;


}
