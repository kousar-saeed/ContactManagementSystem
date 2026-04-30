package com.cms.contactmanagement.service.impl;

import com.cms.contactmanagement.dto.UserRegistrationRequestDto;
import com.cms.contactmanagement.dto.UserRegistrationResponseDto;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.DuplicateEmailException;
import com.cms.contactmanagement.exception.UserNotFoundException;
import com.cms.contactmanagement.exception.ValidationException;
import com.cms.contactmanagement.repository.UserRepository;
import com.cms.contactmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto request) {
        try {
            log.info("Registering user with email={}", request.getEmail());

            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("Registration failed (duplicate email): email={}", request.getEmail());
                throw new DuplicateEmailException("Email already exists");
            }

            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                log.error("Registration failed (duplicate phone): phone={}", request.getPhone());
                throw new ValidationException("Phone already exists");
            }

            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            User saved = userRepository.save(user);
            log.info("User registered successfully: userId={}, email={}", saved.getId(), saved.getEmail());

            return UserRegistrationResponseDto.builder()
                    .id(saved.getId())
                    .firstName(saved.getFirstName())
                    .lastName(saved.getLastName())
                    .email(saved.getEmail())
                    .phone(saved.getPhone())
                    .createdAt(saved.getCreatedAt())
                    .build();
        } catch (RuntimeException ex) {
            log.error("Error registering user: email={}", request.getEmail(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        try {
            log.info("Finding user by email={}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found for email=" + email));
        } catch (RuntimeException ex) {
            log.error("Error finding user by email={}", email, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        try {
            log.info("Finding user by id={}", id);
            return userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found for id=" + id));
        } catch (RuntimeException ex) {
            log.error("Error finding user by id={}", id, ex);
            throw ex;
        }
    }
}

