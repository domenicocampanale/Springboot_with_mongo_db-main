package com.stage.mongodb.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class ReviewDtoUpdate {

    @NotNull(message = "The rating cannot be null")
    @Min(value = 1, message = "The rating must be at least 1")
    @Max(value = 5, message = "The rating must be at most 5")
    private int rating;

    @NotBlank(message = "The comment cannot be empty or contain only spaces")
    @Size(min = 1, max = 100, message = "The comment cannot be too short or too long")
    private String comment;

}
