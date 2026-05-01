package com.cms.contactmanagement.controller;

import com.cms.contactmanagement.config.JwtUtil;
import com.cms.contactmanagement.dto.AuthResponseDto;
import com.cms.contactmanagement.dto.ChangePasswordRequestDto;
import com.cms.contactmanagement.dto.LoginRequestDto;
import com.cms.contactmanagement.dto.UserRegistrationRequestDto;
import com.cms.contactmanagement.dto.UserRegistrationResponseDto;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.ValidationException;
import com.cms.contactmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> register(@Valid @RequestBody UserRegistrationRequestDto request) {
        log.info("Auth register attempt: email={}", request.getEmail());
        UserRegistrationResponseDto created = userService.registerUser(request);
        log.info("Auth register success: userId={}, email={}", created.getId(), created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Auth login attempt: email={}", request.getEmail());

        try {
            User user = userService.findByEmail(request.getEmail());
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.error("Auth login failed (invalid credentials): email={}", request.getEmail());
                throw new ValidationException("Invalid credentials");
            }

            String token = jwtUtil.generateToken(user.getEmail());
            log.info("Auth login success: userId={}, email={}", user.getId(), user.getEmail());

            UserRegistrationResponseDto userDto = UserRegistrationResponseDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .createdAt(user.getCreatedAt())
                    .build();

            return ResponseEntity.ok(AuthResponseDto.builder()
                    .token(token)
                    .user(userDto)
                    .build());
        } catch (RuntimeException ex) {
            log.error("Auth login exception: email={}", request.getEmail(), ex);
            throw ex;
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDto request
    ) {
        String email = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        if (email == null || email.isBlank()) {
            log.error("Change password failed (missing principal)");
            throw new ValidationException("Unauthorized");
        }

        log.info("Auth change-password attempt: email={}", email);
        userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
        log.info("Auth change-password success: email={}", email);
        return ResponseEntity.noContent().build();
    }
}

