package com.cms.contactmanagement.dto;

import com.cms.contactmanagement.entity.PhoneLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhoneNumberRequestDto {
    @NotBlank(message = "Number is required")
    private String number;

    @NotNull(message = "Label is required")
    private PhoneLabel label;
}

