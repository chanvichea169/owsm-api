package com.owsm.AuthService.securityaudit.repository;

import com.owsm.AuthService.securityaudit.entity.SecurityAudit;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.UUID;

public interface SecurityAuditRepository extends JpaRepository<SecurityAudit, Long>, JpaSpecificationExecutor<SecurityAudit> {
    Page<SecurityAudit> findByUser_Id(Long userId, Pageable pageable);
    Page<SecurityAudit> findByUser_IdAndOccurredAtBetween(Long userId, Instant from, Instant to, Pageable pageable);
    Page<SecurityAudit> findByUser_IdAndEventTypeAndStatusAndOccurredAtBetween(Long userId, SecurityEventType eventType, SecurityEventStatus status, Instant from, Instant to, Pageable pageable);
    Page<SecurityAudit> findByEventTypeAndOccurredAtBetween(SecurityEventType eventType, Instant from, Instant to, Pageable pageable);
    Page<SecurityAudit> findByStatusAndOccurredAtBetween(SecurityEventStatus status, Instant from, Instant to, Pageable pageable);
    Page<SecurityAudit> findByDevice_IdAndOccurredAtBetween(UUID deviceId, Instant from, Instant to, Pageable pageable);
    long countByUser_IdAndEventType(Long userId, SecurityEventType eventType);
    long countByUser_IdAndStatusAndOccurredAtBetween(Long userId, SecurityEventStatus status, Instant from, Instant to);
    long countByEventTypeAndStatusAndOccurredAtBetween(SecurityEventType eventType, SecurityEventStatus status, Instant from, Instant to);
    long countByStatusAndOccurredAtBetween(SecurityEventStatus status, Instant from, Instant to);
    long countByDevice_IdAndOccurredAtBetween(UUID deviceId, Instant from, Instant to);
}
