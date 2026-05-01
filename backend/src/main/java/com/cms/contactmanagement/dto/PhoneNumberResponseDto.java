package com.cms.contactmanagement.dto;

import com.cms.contactmanagement.entity.PhoneLabel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhoneNumberResponseDto {
    Long id;
    String number;
    PhoneLabel label;
}

