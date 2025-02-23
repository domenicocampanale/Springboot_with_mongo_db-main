package com.stage.mongodb.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Schema(description = "DTO Review sent in output")
public class ReviewDto {
    @Schema(description = "Review dto id")
    private String id;
    @Schema(description = "Review dto's movieDto")
    private MovieDto movieDto;
    @Schema(description = "Review dto rating")
    private int rating;
    @Schema(description = "Review dto comment")
    private String comment;
    @Schema(description = "Review dto insert date automatically generated")
    private String insertDate;
    @Schema(description = "Review dto update date automatically generated")
    private String updateDate;


}
