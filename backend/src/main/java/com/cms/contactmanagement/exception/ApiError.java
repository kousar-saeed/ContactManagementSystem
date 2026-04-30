package com.cms.contactmanagement.exception;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ApiError {
    Instant timestamp;
    int status;
    String message;
    String path;
}

