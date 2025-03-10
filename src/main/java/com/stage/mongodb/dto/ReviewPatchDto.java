package com.stage.mongodb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewPatchDto {

    private int rating;

    @Size(min = 1, max = 100, message = "The comment cannot be too short or too long")
    private String comment;


}
