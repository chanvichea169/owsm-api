package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.TelegramLinkToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface TelegramLinkTokenRepository extends JpaRepository<TelegramLinkToken, Long> {
    @Modifying
    @Query("DELETE FROM TelegramLinkToken t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TelegramLinkToken t WHERE t.tokenHash = :tokenHash "
            + "AND t.usedAt IS NULL AND t.expiresAt > :now")
    Optional<TelegramLinkToken> findActiveByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now
    );
}