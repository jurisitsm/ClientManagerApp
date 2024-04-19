package com.jurisitsm.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jurisitsm.test.model.AppUser;
import com.jurisitsm.test.model.RefreshToken;
import com.jurisitsm.test.repository.RefreshTokenRepository;
import com.jurisitsm.test.repository.UserRepository;
import com.jurisitsm.test.web.dto.LoginRequest;
import com.jurisitsm.test.web.dto.RefreshTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private AppUser user;
    private String refreshTokenId;
    private static final String TEST_PASSWORD = "password";

    @Autowired
    public AuthControllerIntegrationTest(MockMvc mockMvc, UserRepository userRepository,
                                         RefreshTokenRepository refreshTokenRepository,
                                         PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    public void setup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        user = new AppUser("test@example.com", "Test User", passwordEncoder.encode(TEST_PASSWORD));
        userRepository.save(user);
        RefreshToken refreshToken = refreshTokenRepository.save(new RefreshToken(LocalDateTime.now()
                .plusMinutes(120), user));
        refreshTokenId = refreshToken.getId();
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), TEST_PASSWORD);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    public void testInvalidLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), "NotTheCorrectPassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="test@example.com", roles={"USER"})
    public void testRefreshToken() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshTokenId);
        mockMvc.perform(post("/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
