package com.cms.contactmanagement.dto;

import com.cms.contactmanagement.entity.EmailLabel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailAddressRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Label is required")
    private EmailLabel label;
}

