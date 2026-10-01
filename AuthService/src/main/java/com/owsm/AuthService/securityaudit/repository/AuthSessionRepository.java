package com.owsm.AuthService.securityaudit.repository;

import com.owsm.AuthService.securityaudit.entity.AuthSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
