package com.stage.mongodb.exceptions;

import java.io.Serial;

public class MovieNotFoundException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1458488340100671889L;

    public MovieNotFoundException(String message) {
        super(message);
    }
}
