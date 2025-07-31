package com.codehacks.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blog_users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_role", columnList = "role")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    // Password can be null for magic link users, or hashed for traditional login
    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // Profile picture fields - direct storage instead of reference
    @Column(name = "profile_picture_url")
    private String profilePictureUrl; // URL/path to the profile picture

    @Column(name = "profile_picture_filename")
    private String profilePictureFilename; // Original filename

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return a list of authorities (roles) for the user
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // For our application, email will be used as the primary identifier for login
        // but Spring Security's UserDetails interface expects 'username'.
        // We'll use email here for consistency with login/magic link.
        return email;
    }

    public String getDisplayName() {
        return this.username;
    }

    /**
     * Get the actual username field value (not the email-based username for Spring Security)
     */
    public String getActualUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // For simplicity, accounts never expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // For simplicity, accounts are never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // For simplicity, credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return true; // For simplicity, accounts are always enabled
    }
}

