package com.cms.contactmanagement.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ContactResponseDto {
    Long id;
    String firstName;
    String lastName;
    String title;
    Instant createdAt;
    Instant updatedAt;
    List<EmailAddressResponseDto> emailAddresses;
    List<PhoneNumberResponseDto> phoneNumbers;
}

