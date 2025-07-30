package com.codehacks.config;

import com.codehacks.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail; // Using email as username for Spring Security

        // 1. Check for Authorization header and Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue filter chain
            return;
        }

        // 2. Extract JWT token
        jwt = authHeader.substring(7); // "Bearer " is 7 characters

        // 3. Extract username (email) from token
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Log the exception (e.g., token expired, malformed)
            log.warn("JWT extraction failed: {}", e.getMessage());
            // Optionally, set response status to 401 or 403 here if you want to explicitly reject
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response); // Continue filter chain, let other handlers deal with it
            return;
        }


        // 4. Validate token and set SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // User is not yet authenticated in the current security context
            UserDetails userDetails = this.userService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If token is valid, create an Authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are null for JWT as they are not stored
                        userDetails.getAuthorities() // User roles/authorities
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Set the Authentication object in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("JWT token is invalid for user: {}", userEmail);
            }
        }
        filterChain.doFilter(request, response); // Continue to the next filter in the chain
    }
}

