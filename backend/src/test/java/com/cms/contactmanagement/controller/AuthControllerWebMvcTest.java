package com.cms.contactmanagement.controller;

import com.cms.contactmanagement.config.JwtUtil;
import com.cms.contactmanagement.dto.UserRegistrationResponseDto;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.GlobalExceptionHandler;
import com.cms.contactmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void register_returns201_withValidInput() throws Exception {
        UserRegistrationResponseDto response = UserRegistrationResponseDto.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("1234567890")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(userService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "email": "jane@example.com",
                                  "phone": "1234567890",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void register_returns400_withInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Doe",
                                  "email": "not-an-email",
                                  "phone": "",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns200_withJwtToken() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@example.com");
        user.setPhone("1234567890");
        user.setPasswordHash("hashed");
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(userService.findByEmail(eq("jane@example.com"))).thenReturn(user);
        when(passwordEncoder.matches(eq("password123"), eq("hashed"))).thenReturn(true);
        when(jwtUtil.generateToken(eq("jane@example.com"))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"));
    }

    @Test
    void login_returns401_withWrongPassword() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed");

        when(userService.findByEmail(eq("jane@example.com"))).thenReturn(user);
        when(passwordEncoder.matches(eq("wrongpass"), eq("hashed"))).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@example.com",
                                  "password": "wrongpass"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}

