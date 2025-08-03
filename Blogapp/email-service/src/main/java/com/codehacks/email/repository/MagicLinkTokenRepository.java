package com.codehacks.email.repository;

import com.codehacks.email.model.MagicLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, Long> {

    Optional<MagicLinkToken> findByToken(String token);
    
    Optional<MagicLinkToken> findByEmailAndUsedFalseAndExpiresAtAfter(String email, LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM MagicLinkToken t WHERE t.expiresAt < ?1")
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
    
    @Modifying
    @Query("DELETE FROM MagicLinkToken t WHERE t.email = ?1 AND t.used = false")
    void deleteUnusedTokensByEmail(String email);
} 