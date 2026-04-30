package com.cms.contactmanagement.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException() {
        super("Contact not found");
    }

    public ContactNotFoundException(String message) {
        super(message);
    }
}

