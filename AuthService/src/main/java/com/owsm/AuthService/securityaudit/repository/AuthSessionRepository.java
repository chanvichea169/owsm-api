package com.owsm.AuthService.securityaudit.repository;

import com.owsm.AuthService.securityaudit.entity.AuthSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByTokenId(UUID tokenId);
    Optional<AuthSession> findBySessionIdAndTokenIdAndRevokedAtIsNullAndExpiresAtAfter(
            UUID sessionId, UUID tokenId, Instant now);
    Optional<AuthSession> findBySessionIdAndUser_Id(UUID sessionId, Long userId);
    Page<AuthSession> findByUser_Id(Long userId, Pageable pageable);
    Page<AuthSession> findByUser_IdAndRevokedAtIsNull(Long userId, Pageable pageable);
    long countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(Long userId, Instant now);

    @Modifying
    @Query("update AuthSession session set session.revokedAt = :revokedAt where session.user.id = :userId and session.revokedAt is null")
    int revokeAllActiveByUserId(Long userId, Instant revokedAt);

    @Modifying
    @Query("delete from AuthSession session where session.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
