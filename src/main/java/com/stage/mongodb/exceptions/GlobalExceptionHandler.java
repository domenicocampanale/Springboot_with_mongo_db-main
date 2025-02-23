package com.stage.mongodb.exceptions;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String getCurrentFormattedDate() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private ErrorDetails buildErrorDetails(String message, String description, String type) {
        return new ErrorDetails(getCurrentFormattedDate(), message, description, type);
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleMovieNotFoundException(MovieNotFoundException exception,
                                                                     WebRequest webRequest) {
        log.error("Handled Exception: MovieNotFoundException - {}", exception.getMessage());
        ErrorDetails errorDetails = buildErrorDetails(exception.getMessage(), webRequest.getDescription(false),
                "MOVIE NOT FOUND");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleReviewNotFoundException(ReviewNotFoundException exception,
                                                                      WebRequest webRequest) {
        log.error("Handled Exception: ReviewNotFoundException - {}", exception.getMessage());
        ErrorDetails errorDetails = buildErrorDetails(exception.getMessage(), webRequest.getDescription(false),
                "REVIEW NOT FOUND");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
                                                                              WebRequest webRequest) {
        log.error("Handled Exception: MethodArgumentNotValidException - {}", exception.getMessage());
        ErrorDetails errorDetails = buildErrorDetails("ONE OR MORE FIELDS DO NOT RESPECT VALIDATION",
                webRequest.getDescription(false), "BAD REQUEST, ARGUMENT NOT VALID");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException exception,
                                                                       WebRequest webRequest) {
        log.error("Handled Exception: IllegalArgumentException  - {}", exception.getMessage());
        ErrorDetails errorDetails = buildErrorDetails("ONE OR MORE FIELDS ARE ILLEGAL ARGUMENTS",
                webRequest.getDescription(false), "BAD REQUEST, ARGUMENT NOT VALID");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception exception, WebRequest webRequest) {
        log.error("Handling generic exception: {} - {}", exception.getClass().getSimpleName(), exception.getMessage());
        ErrorDetails errorDetails = buildErrorDetails(exception.getMessage(), webRequest.getDescription(false),
                "INTERNAL SERVER ERROR");
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
