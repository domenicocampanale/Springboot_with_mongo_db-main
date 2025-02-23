package com.stage.mongodb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
@Schema(description = "DTO Movie sent in output")
public class MovieDto {
    @Schema(description = "Movie dto id")
    private String id;
    @Schema(description = "Movie dto title")
    private String title;
    @Schema(description = "Movie dto release date")
    private String releaseDate;
    @Schema(description = "Movie dto insert date automatically generated")
    private String insertDate;
    @Schema(description = "Movie dto update date automatically generated")
    private String updateDate;
}
