package com.cms.contactmanagement.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserRegistrationResponseDto {
    Long id;
    String firstName;
    String lastName;
    String email;
    String phone;
    Instant createdAt;
}

