package com.codehacks.auth.controller;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.auth.service.AuthService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();;

    private User testUser;
    private UserCreateRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);

        validRegisterRequest = new UserCreateRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setFirstName("New");
        validRegisterRequest.setLastName("User");
        validRegisterRequest.setEmail("new@example.com");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");

        // Setup SecurityContext mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void register_shouldReturnCreatedUser_whenValidRequest() throws Exception {
        // Given
        when(authService.registerUser(any(UserCreateRequest.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        // Given
        when(authService.registerUser(any(UserCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("User with this email already exists."));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with this email already exists."));

        verify(authService).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameAlreadyExists() throws Exception {
        // Given
        when(authService.registerUser(any(UserCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is already taken."));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken."));

        verify(authService).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        // Given
        when(authService.registerUser(any(UserCreateRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Registration failed: Database connection failed"));

        verify(authService).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");
        invalidRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenMissingRequiredFields() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("testuser");
        // Missing firstName, lastName, email

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void login_shouldReturnSuccessMessage_whenValidRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Magic link sent to your email (simulated)."));

        // Note: The login endpoint doesn't call authService, it just logs and returns a message
    }

    @Test
    void login_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMagicLink_shouldReturnJwtToken_whenValidEmail() throws Exception {
        // Given
        String email = "test@example.com";
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(authService.verifyMagicLinkAndLogin(email)).thenReturn(jwtToken);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-magic-link")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string(jwtToken));

        verify(authService).verifyMagicLinkAndLogin(email);
    }

    @Test
    void verifyMagicLink_shouldReturnBadRequest_whenUserNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        when(authService.verifyMagicLinkAndLogin(email))
                .thenThrow(new IllegalArgumentException("User not found with email: " + email));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-magic-link")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with email: " + email));

        verify(authService).verifyMagicLinkAndLogin(email);
    }

    @Test
    void verifyMagicLink_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        // Given
        String email = "test@example.com";
        when(authService.verifyMagicLinkAndLogin(email))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-magic-link")
                        .param("email", email))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Magic link verification failed: Database connection failed"));

        verify(authService).verifyMagicLinkAndLogin(email);
    }

    @Test
    void refreshToken_shouldReturnNewToken_whenAuthenticated() throws Exception {
        // Given
        String newToken = "new-jwt-token";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authService.refreshToken(authentication)).thenReturn(newToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(newToken));

        verify(authService).refreshToken(authentication);
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("No authenticated user to refresh token for."));

        verify(authService, never()).refreshToken(any(Authentication.class));
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenAuthenticationIsNull() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("No authenticated user to refresh token for."));

        verify(authService, never()).refreshToken(any(Authentication.class));
    }

    @Test
    void refreshToken_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authService.refreshToken(authentication))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Token refresh failed: Token generation failed"));

        verify(authService).refreshToken(authentication);
    }

    @Test
    void register_shouldValidateUsernameLength() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("ab"); // Too short (min 3)
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");
        invalidRequest.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldValidateUsernameMaxLength() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("a".repeat(51)); // Too long (max 50)
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");
        invalidRequest.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldValidateFirstNameMaxLength() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setFirstName("a".repeat(51)); // Too long (max 50)
        invalidRequest.setLastName("User");
        invalidRequest.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }

    @Test
    void register_shouldValidateLastNameMaxLength() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("a".repeat(51)); // Too long (max 50)
        invalidRequest.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(UserCreateRequest.class));
    }
} 