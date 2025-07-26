package com.codehacks.auth.service;

import com.codehacks.auth.dto.LoginRequest;
import com.codehacks.config.JwtService;
import com.codehacks.user.dto.UserCreateRequest;
import com.codehacks.user.model.User;
import com.codehacks.user.model.UserRole;
import com.codehacks.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User registerUser(UserCreateRequest request) {

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }


        User newUser = new User();
        newUser.setUsername(request.getUsername()); // Set the display username
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setRole(UserRole.USER);
        newUser.setPassword(null); // Password will be set upon magic link verification or initial setup

        User savedUser = userService.saveUser(newUser);

        System.out.println("Simulating magic link generation for: " + savedUser.getEmail());

        return savedUser;
    }

    // This method simulates the magic link login process.
    @Transactional
    public String verifyMagicLinkAndLogin(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Load user details using email (which is the UserDetails username)
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        // Generate JWT token for the authenticated user
        String jwtToken = jwtService.generateToken(userDetails);

        // Set authentication in SecurityContext (optional for stateless, but good for immediate use)
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return jwtToken;
    }

    // This method is for traditional username/password login (if you decide to add it later)
    public String authenticate(LoginRequest request) {
        // Authenticate using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), "magic_link_placeholder_password")
        );

        // If authentication is successful, generate JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    public String refreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}