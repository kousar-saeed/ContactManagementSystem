package com.cms.contactmanagement.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponseDto {
    String token;
    UserRegistrationResponseDto user;
}

