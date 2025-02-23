package com.stage.mongodb.exceptions;

import java.io.Serial;

public class ReviewNotFoundException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1458488340100671889L;

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
