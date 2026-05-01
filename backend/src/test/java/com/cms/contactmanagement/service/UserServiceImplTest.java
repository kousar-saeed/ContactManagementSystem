package com.cms.contactmanagement.service;

import com.cms.contactmanagement.dto.UserRegistrationRequestDto;
import com.cms.contactmanagement.dto.UserRegistrationResponseDto;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.DuplicateEmailException;
import com.cms.contactmanagement.exception.UserNotFoundException;
import com.cms.contactmanagement.repository.UserRepository;
import com.cms.contactmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new UserRegistrationRequestDto();
        validRequest.setFirstName("Jane");
        validRequest.setLastName("Doe");
        validRequest.setEmail("jane@example.com");
        validRequest.setPhone("1234567890");
        validRequest.setPassword("password123");
    }

    @Test
    void registerUser_happyPath() {
        when(userRepository.existsByEmail(eq("jane@example.com"))).thenReturn(false);
        when(userRepository.findByPhone(eq("1234567890"))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(eq("password123"))).thenReturn("hashed");

        User saved = new User();
        saved.setId(1L);
        saved.setFirstName("Jane");
        saved.setLastName("Doe");
        saved.setEmail("jane@example.com");
        saved.setPhone("1234567890");
        saved.setPasswordHash("hashed");
        saved.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserRegistrationResponseDto response = userService.registerUser(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhone());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.getCreatedAt());

        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).findByPhone("1234567890");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_throwsDuplicateEmailException_whenEmailExists() {
        when(userRepository.existsByEmail(eq("jane@example.com"))).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(validRequest));

        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_returnsCorrectUser() {
        User user = new User();
        user.setId(10L);
        user.setEmail("jane@example.com");

        when(userRepository.findByEmail(eq("jane@example.com"))).thenReturn(Optional.of(user));

        User found = userService.findByEmail("jane@example.com");

        assertEquals(10L, found.getId());
        assertEquals("jane@example.com", found.getEmail());
        verify(userRepository).findByEmail("jane@example.com");
    }

    @Test
    void findByEmail_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findByEmail(eq("missing@example.com"))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("missing@example.com"));

        verify(userRepository).findByEmail("missing@example.com");
    }
}

