package com.stage.mongodb.controller;

import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.dto.ReviewPatchDto;
import com.stage.mongodb.exceptions.ErrorDetails;
import com.stage.mongodb.service.ReviewService;
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
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping("/all")
    @Operation(description = "find all reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of reviews correctly viewed",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewDto.class)))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<List<ReviewDto>> getReviews() {
        log.info("Request for showing review list");
        List<ReviewDto> reviewsDto = reviewService.getReviews();
        return ResponseEntity.status(HttpStatus.OK).body(reviewsDto);
    }

    @GetMapping(params = "id")
    @Operation(description = "find review by corresponding id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<ReviewDto> getReviewById(@RequestParam String id) {
        log.info("Request for showing review with id {} ", id);
        ReviewDto reviewDto = reviewService.getReviewById(id);
        return ResponseEntity.status(HttpStatus.OK).body(reviewDto);
    }

    @PostMapping
    @Operation(description = "insert a review into the review collection")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Review correctly created",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ReviewDto.class))),
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
    public ResponseEntity<ReviewDto> insert(@Valid @RequestBody ReviewDtoInput reviewDtoInput) {
        log.info("Request for inserting review");
        ReviewDto reviewDto = reviewService.insertReview(reviewDtoInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    @PutMapping(params = "id")
    @Operation(description = "update an existing review")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Review correctly updated",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ReviewDto.class))),
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
    public ResponseEntity<ReviewDto> update(@RequestParam String id, @Valid @RequestBody ReviewDtoUpdate reviewDtoUpdate) {
        log.info("Request for updating review with id {}", id);
        ReviewDto reviewDto = reviewService.updateReview(reviewDtoUpdate, id);
        return ResponseEntity.status(HttpStatus.OK).body(reviewDto);
    }

    @PatchMapping(params = "id")
    @Operation(description = "update an existing review by not passing all of the attributes")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Review correctly updated",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ReviewDto.class))),
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
    public ResponseEntity<ReviewDto> updateReviewPartial(@RequestParam String id,
                                                         @Valid @RequestBody ReviewPatchDto reviewPatchDto) {
        log.info("Request for partial update of review with id {}", id);
        ReviewDto reviewDto = reviewService.updateReviewPartial(id, reviewPatchDto);
        return ResponseEntity.status(HttpStatus.OK).body(reviewDto);
    }

    @DeleteMapping(params = "id")
    @Operation(description = "delete an existing review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review correctly deleted"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    public void delete(@RequestParam String id) {
        log.info("Request for deleting review with id {}", id);
        reviewService.deleteReview(id);
    }
}
