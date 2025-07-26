package com.codehacks.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void loginRequest_shouldBeValid_whenValidEmail() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_shouldBeInvalid_whenEmailIsNull() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(null);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    void loginRequest_shouldBeInvalid_whenEmailIsEmpty() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    void loginRequest_shouldBeInvalid_whenEmailIsBlank() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("   ");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Email will trigger
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email cannot be empty"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Invalid email format"));
    }

    @Test
    void loginRequest_shouldBeInvalid_whenEmailFormatIsInvalid() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void loginRequest_shouldBeInvalid_whenEmailHasNoDomain() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void registerRequest_shouldBeValid_whenAllFieldsAreValid() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_shouldBeInvalid_whenUsernameIsNull() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(null);
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenUsernameIsEmpty() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Size will trigger
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Username cannot be empty"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Username must be between 3 and 50 characters"));
    }

    @Test
    void registerRequest_shouldBeInvalid_whenUsernameIsTooShort() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // Less than 3 characters
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenUsernameIsTooLong() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(51)); // More than 50 characters
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenFirstNameIsNull() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName(null);
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("First name cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenFirstNameIsEmpty() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("First name cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenFirstNameIsTooLong() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("a".repeat(51)); // More than 50 characters
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("First name cannot exceed 50 characters");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenLastNameIsNull() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName(null);
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Last name cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenLastNameIsEmpty() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Last name cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenLastNameIsTooLong() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("a".repeat(51)); // More than 50 characters
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Last name cannot exceed 50 characters");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenEmailIsNull() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenEmailIsEmpty() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenEmailFormatIsInvalid() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void registerRequest_shouldBeInvalid_whenMultipleFieldsAreInvalid() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // Too short
        request.setFirstName(""); // Empty
        request.setLastName("a".repeat(51)); // Too long
        request.setEmail("invalid-email"); // Invalid format

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(4);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Username must be between 3 and 50 characters"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("First name cannot be empty"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Last name cannot exceed 50 characters"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Invalid email format"));
    }

    @Test
    void registerRequest_shouldBeValid_whenUsernameIsExactlyMinLength() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc"); // Exactly 3 characters
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_shouldBeValid_whenUsernameIsExactlyMaxLength() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(50)); // Exactly 50 characters
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_shouldBeValid_whenFirstNameIsExactlyMaxLength() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("a".repeat(50)); // Exactly 50 characters
        request.setLastName("User");
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_shouldBeValid_whenLastNameIsExactlyMaxLength() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("a".repeat(50)); // Exactly 50 characters
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
} 