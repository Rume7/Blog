package com.codehacks.auth.service;

import com.codehacks.email.client.EmailServiceClient;
import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.config.JwtService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private EmailServiceClient emailServiceClient;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserCreateRequest validRequest;
    private LoginRequest loginRequest;

    private static final String MAGIC_LINK_TOKEN_PREFIX = "magic_link:";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);

        validRequest = new UserCreateRequest();
        validRequest.setUsername("newuser");
        validRequest.setFirstName("New");
        validRequest.setLastName("User");
        validRequest.setEmail("new@example.com");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");

        // Setup SecurityContext mock - only for tests that need it
        // when(securityContext.getAuthentication()).thenReturn(authentication);
        // SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerUser_shouldCreateNewUser_whenValidRequest() {
        // Given
        when(userService.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userService.findByUsername(validRequest.getUsername())).thenReturn(Optional.empty());
        when(userService.saveUser(any(User.class))).thenReturn(testUser);

        // When
        User result = authService.registerUser(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getPassword()).isNull(); // Password should be null for magic link auth

        verify(userService).findByEmail(validRequest.getEmail());
        verify(userService).findByUsername(validRequest.getUsername());
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        when(userService.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with this email already exists.");

        verify(userService).findByEmail(validRequest.getEmail());
        verify(userService, never()).findByUsername(anyString());
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenUsernameAlreadyExists() {
        // Given
        when(userService.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userService.findByUsername(validRequest.getUsername())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken.");

        verify(userService).findByEmail(validRequest.getEmail());
        verify(userService).findByUsername(validRequest.getUsername());
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void registerUser_shouldSetCorrectUserProperties() {
        // Given
        when(userService.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userService.findByUsername(validRequest.getUsername())).thenReturn(Optional.empty());
        
        // Capture the user that gets saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.saveUser(userCaptor.capture())).thenAnswer(invocation -> {
            User savedUser = userCaptor.getValue();
            // Set the ID to simulate a saved user
            savedUser.setId(1L);
            return savedUser;
        });

        // When
        User result = authService.registerUser(validRequest);

        // Then
        assertThat(result).isNotNull();
        // The result is the captured user, so check its properties
        User capturedUser = userCaptor.getValue();
        // Check the actual username field, not the overridden getUsername() method
        assertThat(capturedUser.getActualUsername()).isEqualTo("newuser");
        assertThat(capturedUser.getFirstName()).isEqualTo("New");
        assertThat(capturedUser.getLastName()).isEqualTo("User");
        assertThat(capturedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedUser.getPassword()).isNull();
    }

    @Test
    void initiateMagicLinkLogin_shouldSendEmail_whenValidRequest() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When
        authService.initiateMagicLinkLogin(loginRequest);

        // Then
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(emailServiceClient).sendMagicLinkEmail(any());
    }

    @Test
    void initiateMagicLinkLogin_shouldThrowException_whenUserNotFound() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.initiateMagicLinkLogin(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found with email: " + loginRequest.getEmail());

        verify(userService).findByEmail(loginRequest.getEmail());
        verify(emailServiceClient, never()).sendMagicLinkEmail(any());
    }

    @Test
    void verifyMagicLinkAndLogin_shouldReturnJwtToken_whenValidEmail() {
        // Given
        String token = "valid-token";
        String email = "test@example.com";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(MAGIC_LINK_TOKEN_PREFIX + token)).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userService.loadUserByUsername(email)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        
        // Mock cache manager
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("users")).thenReturn(mockCache);

        // When
        String result = authService.verifyMagicLinkAndLogin(token);

        // Then
        assertThat(result).isEqualTo("jwt-token");
        verify(redisTemplate.opsForValue()).get(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(redisTemplate).delete(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(userService).findByEmail(email);
        verify(userService).loadUserByUsername(email);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void verifyMagicLinkAndLogin_shouldThrowException_whenUserNotFound() {
        // Given
        String token = "valid-token";
        String email = "nonexistent@example.com";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(MAGIC_LINK_TOKEN_PREFIX + token)).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.verifyMagicLinkAndLogin(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(redisTemplate.opsForValue()).get(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(redisTemplate).delete(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(userService).findByEmail(email);
        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void verifyMagicLinkAndLogin_shouldThrowException_whenInvalidToken() {
        // Given
        String token = "invalid-token";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(MAGIC_LINK_TOKEN_PREFIX + token)).thenReturn(null);
        when(emailServiceClient.validateMagicLinkToken(token)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.verifyMagicLinkAndLogin(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired magic link token");

        verify(redisTemplate.opsForValue()).get(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(emailServiceClient).validateMagicLinkToken(token);
        verify(emailServiceClient, never()).getEmailFromToken(anyString());
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void verifyMagicLinkAndLogin_shouldThrowException_whenEmailNotFound() {
        // Given
        String token = "valid-token";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(MAGIC_LINK_TOKEN_PREFIX + token)).thenReturn(null);
        when(emailServiceClient.validateMagicLinkToken(token)).thenReturn(true);
        when(emailServiceClient.getEmailFromToken(token)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.verifyMagicLinkAndLogin(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid magic link token");

        verify(redisTemplate.opsForValue()).get(MAGIC_LINK_TOKEN_PREFIX + token);
        verify(emailServiceClient).validateMagicLinkToken(token);
        verify(emailServiceClient).getEmailFromToken(token);
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void authenticate_shouldReturnJwtToken_whenValidCredentials() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // When
        String result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void authenticate_shouldUseMagicLinkPlaceholderPassword() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // When
        authService.authenticate(loginRequest);

        // Then
        verify(authenticationManager).authenticate(argThat(token -> 
                token instanceof UsernamePasswordAuthenticationToken &&
                ((UsernamePasswordAuthenticationToken) token).getCredentials().equals("magicLinkPlaceholderPassword")
        ));
    }

    @Test
    void refreshToken_shouldReturnNewJwtToken() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new-jwt-token");

        // When
        String result = authService.refreshToken(authentication);

        // Then
        assertThat(result).isEqualTo("new-jwt-token");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void refreshToken_shouldExtractUserDetailsFromAuthentication() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new-jwt-token");

        // When
        authService.refreshToken(authentication);

        // Then
        verify(authentication).getPrincipal();
        verify(jwtService).generateToken(userDetails);
    }
} 