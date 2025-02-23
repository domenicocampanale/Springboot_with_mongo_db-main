package com.stage.mongodb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class MovieDtoInput {

    @NotBlank(message = "The title cannot be empty or contain only spaces")
    @Size(min = 1, max = 100, message = "The title cannot be too short or too long")
    private String title;

    @NotBlank(message = "The release date cannot be empty or contain only spaces")
    @Size(min = 1, max = 100, message = "The release date cannot be too short or too long")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "The release date must follow the format YYYY-MM-DD")
    private String releaseDate;


}