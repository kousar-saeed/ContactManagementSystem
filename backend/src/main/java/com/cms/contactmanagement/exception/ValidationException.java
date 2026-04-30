package com.cms.contactmanagement.exception;

public class ValidationException extends RuntimeException {
    public ValidationException() {
        super("Validation error");
    }

    public ValidationException(String message) {
        super(message);
    }
}

