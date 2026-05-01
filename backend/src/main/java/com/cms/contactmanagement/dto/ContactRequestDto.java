package com.cms.contactmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContactRequestDto {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String title;

    @Valid
    @NotNull(message = "Email addresses list is required")
    private List<EmailAddressRequestDto> emailAddresses = new ArrayList<>();

    @Valid
    @NotNull(message = "Phone numbers list is required")
    private List<PhoneNumberRequestDto> phoneNumbers = new ArrayList<>();
}

