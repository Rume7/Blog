package com.codehacks.user.service;

import com.codehacks.user.model.User;
import com.codehacks.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'all'")
    public List<User> findAllUsers() {
        log.debug("Loading all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        log.debug("Loading user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Transactional
    @Caching(
        put = @CachePut(value = "users", key = "#result.id"),
        evict = {
            @CacheEvict(value = "users", key = "#user.email"),
            @CacheEvict(value = "users", key = "#user.username"),
            @CacheEvict(value = "users", key = "'all'")
        }
    )
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        // Validate email uniqueness if this is a new user
        if (user.getId() == null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Attempted to save user with existing email: {}", user.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        User savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users", key = "'all'")
        }
    )
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent user with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);
    }

    @Transactional
    @Caching(
        put = @CachePut(value = "users", key = "#id"),
        evict = {
            @CacheEvict(value = "users", key = "'all'")
        }
    )
    public User updateUser(Long id, User updatedUser) {
        log.debug("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempted to update non-existent user with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });
        
        // Update fields
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setUsername(updatedUser.getUsername());
        
        User savedUser = userRepository.save(existingUser);
        log.info("User updated with ID: {}", savedUser.getId());
        return savedUser;
    }
}

