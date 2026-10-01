package com.owsm.AuthService.securityaudit.repository;

import com.owsm.AuthService.securityaudit.entity.LoginAudit;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.UUID;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long>, JpaSpecificationExecutor<LoginAudit> {
    Page<LoginAudit> findByUser_Id(Long userId, Pageable pageable);
    Page<LoginAudit> findByUser_IdAndOccurredAtBetween(Long userId, Instant from, Instant to, Pageable pageable);
    Page<LoginAudit> findByUser_IdAndStatusAndOccurredAtBetween(Long userId, LoginStatus status, Instant from, Instant to, Pageable pageable);
    Page<LoginAudit> findByStatusAndOccurredAtBetween(LoginStatus status, Instant from, Instant to, Pageable pageable);
    Page<LoginAudit> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<LoginAudit> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<LoginAudit> findBySessionId(UUID sessionId, Pageable pageable);
    Page<LoginAudit> findByTokenId(UUID tokenId, Pageable pageable);
    java.util.Optional<LoginAudit> findFirstBySessionIdAndStatusOrderByOccurredAtDesc(
            UUID sessionId, LoginStatus status);
    Page<LoginAudit> findBySuspiciousTrueAndOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    long countByUser_IdAndStatus(Long userId, LoginStatus status);
    long countByUser_IdAndStatusAndOccurredAtBetween(Long userId, LoginStatus status, Instant from, Instant to);
    long countByStatusAndOccurredAtBetween(LoginStatus status, Instant from, Instant to);
    long countByUsernameAndOccurredAtBetween(String username, Instant from, Instant to);
    long countByEmailAndOccurredAtBetween(String email, Instant from, Instant to);
    long countBySuspiciousTrueAndOccurredAtBetween(Instant from, Instant to);
}
