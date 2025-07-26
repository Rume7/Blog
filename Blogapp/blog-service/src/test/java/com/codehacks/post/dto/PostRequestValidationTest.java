package com.codehacks.post.dto;

import com.codehacks.post.model.PostStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PostRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validPostRequest_shouldHaveNoViolations() {
        PostRequest req = new PostRequest();
        req.setTitle("Valid Title");
        req.setContent("Some content");
        req.setImageUrl("http://img");
        req.setStatus(PostStatus.PUBLISHED);
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void title_shouldNotBeBlank() {
        PostRequest req = new PostRequest();
        req.setTitle("");
        req.setContent("Content");
        req.setStatus(PostStatus.DRAFT);
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void title_shouldNotExceedMaxLength() {
        PostRequest req = new PostRequest();
        req.setTitle("a".repeat(256));
        req.setContent("Content");
        req.setStatus(PostStatus.DRAFT);
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void content_shouldNotBeBlank() {
        PostRequest req = new PostRequest();
        req.setTitle("Title");
        req.setContent("");
        req.setStatus(PostStatus.DRAFT);
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void status_shouldNotBeNull() {
        PostRequest req = new PostRequest();
        req.setTitle("Title");
        req.setContent("Content");
        req.setStatus(null);
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }
} 