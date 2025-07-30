package com.codehacks.config;

import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set a test secret key (base64 encoded)
        ReflectionTestUtils.setField(jwtService, "secretKey", 
            "ZmFrZV9zZWNyZXRfa2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX29ubHlfZG9udF91c2VfaW5fcHJvZHVjdGlvbg==");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.USER);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateTokenWithExtraClaims_shouldCreateValidToken() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("userId", 1L);

        // When
        String token = jwtService.generateToken(extraClaims, testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo(testUser.getEmail());
    }

    @Test
    void extractUsername_withInvalidToken_shouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractClaim_shouldReturnCorrectClaim() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo(testUser.getEmail()); // JWT subject is email
    }

    @Test
    void extractClaim_withExpiration_shouldReturnCorrectDate() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date()); // Should be in the future
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_withInvalidUser_shouldReturnFalse() {
        // Given
        String token = jwtService.generateToken(testUser);
        User differentUser = new User();
        differentUser.setUsername("differentuser");
        differentUser.setEmail("different@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_withInvalidToken_shouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.isTokenValid(invalidToken, testUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_withDifferentUsers_shouldCreateDifferentTokens() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        // When
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtService.extractUsername(token1)).isEqualTo("user1@example.com");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("user2@example.com");
    }

    @Test
    void generateToken_withExtraClaims_shouldIncludeClaims() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 123L);

        // When
        String token = jwtService.generateToken(extraClaims, testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo(testUser.getEmail());
    }
} 