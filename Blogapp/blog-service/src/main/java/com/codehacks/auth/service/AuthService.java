package com.codehacks.auth.service;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.config.JwtService;
import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.client.EmailServiceClient;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String MAGIC_LINK_TOKEN_PREFIX = "magic_link:";
    private static final Duration MAGIC_LINK_EXPIRATION = Duration.ofMinutes(15); // 15 minutes

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailServiceClient emailServiceClient;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    @CacheEvict(value = "users", key = "#request.email")
    public User registerUser(UserCreateRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("User with this email already exists.");
        }

        if (userService.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username already taken: {}", request.getUsername());
            throw new IllegalArgumentException("Username is already taken.");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setRole(UserRole.USER);
        newUser.setPassword(null); // Password will be set upon magic link verification or initial setup

        User savedUser = userService.saveUser(newUser);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Initiates magic link login process
     */
    public void initiateMagicLinkLogin(LoginRequest request) {
        log.info("Initiating magic link login for: {}", request.getEmail());

        // Check if user exists
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Magic link login failed: User not found: {}", request.getEmail());
                    return new IllegalArgumentException("User not found with email: " + request.getEmail());
                });

        // For testing purposes, create a secure mock magic link token
        // In production, this would call the email service
        try {
            // Try to send magic link email via email service
            MagicLinkEmailRequest emailRequest = new MagicLinkEmailRequest(
                    request.getEmail(),
                    user.getUsername()
            );
            emailServiceClient.sendMagicLinkEmail(emailRequest);
            log.info("Magic link email sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.warn("Email service unavailable, creating secure mock magic link for testing: {}", e.getMessage());
            // Create a secure random token for testing (in production, this should not happen)
            String secureToken = generateSecureToken();
            // Store the token-to-email mapping in Redis with expiration
            storeMagicLinkToken(secureToken, request.getEmail());
            log.info("Secure mock magic link created for testing. Token: {}", secureToken);
            log.info("For testing, you can use this URL: http://localhost:3000/login?token={}", secureToken);
        }
    }

    /**
     * Generates a secure random token for magic links
     */
    private String generateSecureToken() {
        // Generate a cryptographically secure random token
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Stores magic link token in Redis with expiration
     */
    private void storeMagicLinkToken(String token, String email) {
        String redisKey = MAGIC_LINK_TOKEN_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(redisKey, email, MAGIC_LINK_EXPIRATION.toMinutes(), TimeUnit.MINUTES);
            log.debug("Magic link token stored in Redis with key: {}", redisKey);
        } catch (Exception e) {
            log.error("Failed to store magic link token in Redis: {}", e.getMessage());
            throw new RuntimeException("Failed to create magic link", e);
        }
    }

    /**
     * Retrieves and removes magic link token from Redis
     */
    private String retrieveAndRemoveMagicLinkToken(String token) {
        String redisKey = MAGIC_LINK_TOKEN_PREFIX + token;
        try {
            String email = redisTemplate.opsForValue().get(redisKey);
            if (email != null) {
                // Remove the token after retrieval (one-time use)
                redisTemplate.delete(redisKey);
                log.debug("Magic link token retrieved and removed from Redis: {}", redisKey);
                return email;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to retrieve magic link token from Redis: {}", e.getMessage());
            throw new RuntimeException("Failed to verify magic link", e);
        }
    }

    /**
     * Verifies magic link token and logs in user
     */
    @Transactional
    public String verifyMagicLinkAndLogin(String token) {
        log.info("Verifying magic link token");

        String email = retrieveAndRemoveMagicLinkToken(token);
        
        if (email == null) {
            // If not found in Redis, try email service (for production email service tokens)
            if (!emailServiceClient.validateMagicLinkToken(token)) {
                log.warn("Magic link verification failed: Invalid token");
                throw new IllegalArgumentException("Invalid or expired magic link token");
            }

            // Get email from token via email service
            String extractedEmail = emailServiceClient.getEmailFromToken(token);
            if (extractedEmail == null) {
                log.warn("Magic link verification failed: No email found for token");
                throw new IllegalArgumentException("Invalid magic link token");
            }
            email = extractedEmail;
        } else {
            log.info("Magic link token verified via Redis for email: {}", email);
        }

        // Find user by email
        final String finalEmail = email; // Make it effectively final for lambda
        User user = userService.findByEmail(finalEmail)
                .orElseThrow(() -> {
                    log.warn("Magic link verification failed: User not found: {}", finalEmail);
                    return new IllegalArgumentException("User not found");
                });

        // Evict user cache for this email
        cacheManager.getCache("users").evict(finalEmail);

        // Load user details
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Set authentication in SecurityContext
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("Magic link login successful for user: {}", finalEmail);
        return jwtToken;
    }

    /**
     * Traditional username/password login (if implemented later)
     */
    @Cacheable(value = "auth", key = "'token:' + #request.email")
    public String authenticate(LoginRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        // Authenticate using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), "magicLinkPlaceholderPassword")
        );

        // If authentication is successful, generate JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("Traditional authentication successful for user: {}", request.getEmail());
        return jwtToken;
    }

    @Cacheable(value = "auth", key = "'refresh:' + #authentication.name")
    public String refreshToken(Authentication authentication) {
        log.debug("Refreshing token for user: {}", authentication.getName());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}