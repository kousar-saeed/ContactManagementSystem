package com.cms.contactmanagement.service;

import com.cms.contactmanagement.dto.UserRegistrationRequestDto;
import com.cms.contactmanagement.dto.UserRegistrationResponseDto;
import com.cms.contactmanagement.entity.User;

public interface UserService {
    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto request);
    User findByEmail(String email);
    User findById(Long id);
}

