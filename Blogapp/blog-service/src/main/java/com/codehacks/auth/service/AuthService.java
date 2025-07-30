package com.codehacks.auth.service;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.config.JwtService;
import com.codehacks.email.dto.MagicLinkEmailRequest;
import com.codehacks.email.service.EmailService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
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

        // Send magic link email
        MagicLinkEmailRequest emailRequest = new MagicLinkEmailRequest(
                request.getEmail(),
                user.getUsername()
        );

        emailService.sendMagicLinkEmail(emailRequest);
        log.info("Magic link email sent successfully to: {}", request.getEmail());
    }

    /**
     * Verifies magic link token and logs in user
     */
    @Transactional
    public String verifyMagicLinkAndLogin(String token) {
        log.info("Verifying magic link token");

        // Validate the token
        if (!emailService.validateMagicLinkToken(token)) {
            log.warn("Magic link verification failed: Invalid token");
            throw new IllegalArgumentException("Invalid or expired magic link token");
        }

        // Get email from token
        String email = emailService.getEmailFromToken(token);
        if (email == null) {
            log.warn("Magic link verification failed: No email found for token");
            throw new IllegalArgumentException("Invalid magic link token");
        }

        // Find user by email
        User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Magic link verification failed: User not found: {}", email);
                    return new IllegalArgumentException("User not found");
                });

        // Load user details
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Set authentication in SecurityContext
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("Magic link login successful for user: {}", email);
        return jwtToken;
    }

    /**
     * Traditional username/password login (if implemented later)
     */
    public String authenticate(LoginRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        // Authenticate using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), "magic_link_placeholder_password")
        );

        // If authentication is successful, generate JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("Traditional authentication successful for user: {}", request.getEmail());
        return jwtToken;
    }

    public String refreshToken(Authentication authentication) {
        log.debug("Refreshing token for user: {}", authentication.getName());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}