package com.stage.mongodb.exceptions;

public record ErrorDetails(String timestamp, String message, String details, String error_code) {

}
