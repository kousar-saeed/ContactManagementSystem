package com.cms.contactmanagement.dto;

import com.cms.contactmanagement.entity.EmailLabel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EmailAddressResponseDto {
    Long id;
    String email;
    EmailLabel label;
}

