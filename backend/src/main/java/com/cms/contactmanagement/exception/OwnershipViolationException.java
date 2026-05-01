package com.cms.contactmanagement.exception;

public class OwnershipViolationException extends RuntimeException {
    public OwnershipViolationException() {
        super("Forbidden");
    }

    public OwnershipViolationException(String message) {
        super(message);
    }
}

