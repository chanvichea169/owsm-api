package com.owsm.AuthService.securityaudit.service;

import com.owsm.AuthService.securityaudit.dto.LoginAuditResponse;
import com.owsm.AuthService.securityaudit.dto.SecurityEventResponse;
import com.owsm.AuthService.securityaudit.entity.LoginAudit;
import com.owsm.AuthService.securityaudit.entity.SecurityAudit;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventStatus;
import com.owsm.AuthService.securityaudit.repository.LoginAuditRepository;
import com.owsm.AuthService.securityaudit.repository.SecurityAuditRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SecurityAuditReadService {
    private static final List<String> LOGIN_SORT_FIELDS =
            List.of("id", "occurredAt", "status", "riskScore", "method", "ipAddress", "deviceName", "application");
    private static final List<String> EVENT_SORT_FIELDS =
            List.of("id", "occurredAt", "status", "eventType", "ipAddress");
    private final LoginAuditRepository loginAuditRepository;
    private final SecurityAuditRepository securityAuditRepository;

    public SecurityAuditReadService(
            LoginAuditRepository loginAuditRepository,
            SecurityAuditRepository securityAuditRepository
    ) {
        this.loginAuditRepository = loginAuditRepository;
        this.securityAuditRepository = securityAuditRepository;
    }

    public Page<LoginAuditResponse> loginAudits(
            Long userId, String username, String email, String ipAddress, String status,
            String dateFrom, String dateTo, String suspicious, String device, String application,
            Integer page, Integer size, String sort, String direction
    ) {
        Pageable pageable = AuditQuerySupport.pageable(page, size, sort, direction, LOGIN_SORT_FIELDS);
        Specification<LoginAudit> specification = loginAuditSpecification(
                userId, username, email, ipAddress, status, dateFrom, dateTo, suspicious, device, application
        );
        return loginAuditRepository.findAll(specification, pageable).map(LoginAuditResponse::from);
    }

    public LoginAuditResponse loginAudit(Long id) {
        return loginAuditRepository.findById(id)
                .map(LoginAuditResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Login audit not found"));
    }

    public Page<LoginAuditResponse> userLoginHistory(
            Long userId, String username, String email, String ipAddress, String status,
            String dateFrom, String dateTo, String suspicious, String device, String application,
            Integer page, Integer size, String sort, String direction
    ) {
        return loginAudits(userId, username, email, ipAddress, status, dateFrom, dateTo, suspicious,
                device, application, page, size, sort, direction);
    }

    public Page<SecurityEventResponse> securityEvents(
            Long userId, String username, String email, String ipAddress, String status,
            String dateFrom, String dateTo, String suspicious, String device, String application,
            Integer page, Integer size, String sort, String direction
    ) {
        Pageable pageable = AuditQuerySupport.pageable(page, size, sort, direction, EVENT_SORT_FIELDS);
        Specification<SecurityAudit> specification = securityAuditSpecification(
                userId, username, email, ipAddress, status, dateFrom, dateTo, suspicious, device, application
        );
        return securityAuditRepository.findAll(specification, pageable).map(SecurityEventResponse::from);
    }

    public SecurityEventResponse securityEvent(Long id) {
        return securityAuditRepository.findById(id)
                .map(SecurityEventResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Security event not found"));
    }

    private Specification<LoginAudit> loginAuditSpecification(
            Long userId, String username, String email, String ipAddress, String status,
            String dateFrom, String dateTo, String suspicious, String device, String application
    ) {
        LoginStatus loginStatus = AuditQuerySupport.enumValue(LoginStatus.class, status, "status");
        InetAddress ip = AuditQuerySupport.ipAddress(ipAddress);
        Instant from = AuditQuerySupport.dateBound(dateFrom, false);
        Instant to = AuditQuerySupport.dateBound(dateTo, true);
        AuditQuerySupport.validateDateRange(from, to);
        Boolean suspiciousValue = booleanValue(suspicious, "suspicious");
        String normalizedUsername = AuditQuerySupport.normalized(username);
        String normalizedEmail = AuditQuerySupport.normalized(email);
        String normalizedDevice = AuditQuerySupport.normalized(device);
        String normalizedApplication = AuditQuerySupport.normalized(application);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (normalizedUsername != null) {
                predicates.add(contains(cb, root.get("username"), normalizedUsername));
            }
            if (normalizedEmail != null) {
                predicates.add(contains(cb, root.get("email"), normalizedEmail));
            }
            if (ip != null) {
                predicates.add(cb.equal(root.get("ipAddress"), ip));
            }
            if (loginStatus != null) {
                predicates.add(cb.equal(root.get("status"), loginStatus));
            }
            addDatePredicates(root, cb, predicates, from, to);
            if (suspiciousValue != null) {
                predicates.add(suspiciousValue
                        ? cb.isTrue(root.get("suspicious"))
                        : cb.isFalse(root.get("suspicious")));
            }
            if (normalizedDevice != null) {
                predicates.add(cb.or(
                        contains(cb, root.get("deviceName"), normalizedDevice),
                        contains(cb, root.get("deviceType"), normalizedDevice),
                        contains(cb, root.get("deviceId"), normalizedDevice),
                        contains(cb, root.get("userAgent"), normalizedDevice),
                        contains(cb, root.get("browser"), normalizedDevice),
                        contains(cb, root.get("operatingSystem"), normalizedDevice),
                        contains(cb, root.get("platform"), normalizedDevice)
                ));
            }
            if (normalizedApplication != null) {
                predicates.add(contains(cb, root.get("application"), normalizedApplication));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SecurityAudit> securityAuditSpecification(
            Long userId, String username, String email, String ipAddress, String status,
            String dateFrom, String dateTo, String suspicious, String device, String application
    ) {
        SecurityEventStatus eventStatus = AuditQuerySupport.enumValue(SecurityEventStatus.class, status, "status");
        InetAddress ip = AuditQuerySupport.ipAddress(ipAddress);
        Instant from = AuditQuerySupport.dateBound(dateFrom, false);
        Instant to = AuditQuerySupport.dateBound(dateTo, true);
        AuditQuerySupport.validateDateRange(from, to);
        Boolean suspiciousValue = booleanValue(suspicious, "suspicious");
        String normalizedUsername = AuditQuerySupport.normalized(username);
        String normalizedEmail = AuditQuerySupport.normalized(email);
        String normalizedDevice = AuditQuerySupport.normalized(device);
        String normalizedApplication = AuditQuerySupport.normalized(application);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (normalizedUsername != null || normalizedEmail != null) {
                var user = root.join("user", JoinType.LEFT);
                if (normalizedUsername != null) {
                    predicates.add(contains(cb, user.get("username"), normalizedUsername));
                }
                if (normalizedEmail != null) {
                    predicates.add(contains(cb, user.get("email"), normalizedEmail));
                }
            }
            if (ip != null) {
                predicates.add(cb.equal(root.get("ipAddress"), ip));
            }
            if (eventStatus != null) {
                predicates.add(cb.equal(root.get("status"), eventStatus));
            }
            addDatePredicates(root, cb, predicates, from, to);
            if (suspiciousValue != null) {
                var metadataSuspicious = cb.function(
                        "jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal("suspicious")
                );
                Predicate detected = cb.equal(root.get("status"), SecurityEventStatus.DETECTED);
                Predicate metadataFlag = cb.equal(cb.lower(metadataSuspicious), "true");
                if (suspiciousValue) {
                    predicates.add(cb.or(detected, metadataFlag));
                } else {
                    predicates.add(cb.and(
                            cb.not(detected),
                            cb.or(cb.isNull(metadataSuspicious), cb.notEqual(cb.lower(metadataSuspicious), "true"))
                    ));
                }
            }
            if (normalizedDevice != null) {
                var metadataDevice = cb.function(
                        "jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal("deviceType")
                );
                var metadataDeviceName = cb.function(
                        "jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal("deviceName")
                );
                var deviceJoin = root.join("device", JoinType.LEFT);
                predicates.add(cb.or(
                        contains(cb, root.get("userAgent"), normalizedDevice),
                        contains(cb, metadataDevice, normalizedDevice),
                        contains(cb, metadataDeviceName, normalizedDevice),
                        contains(cb, deviceJoin.get("displayName"), normalizedDevice),
                        contains(cb, deviceJoin.get("deviceType"), normalizedDevice),
                        contains(cb, deviceJoin.get("platform"), normalizedDevice),
                        contains(cb, deviceJoin.get("userAgent"), normalizedDevice)
                ));
            }
            if (normalizedApplication != null) {
                var metadataApplication = cb.function(
                        "jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal("application")
                );
                predicates.add(contains(cb, metadataApplication, normalizedApplication));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addDatePredicates(
            jakarta.persistence.criteria.Root<?> root,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            List<Predicate> predicates,
            Instant from,
            Instant to
    ) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }
    }

    private static Predicate contains(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String> expression,
            String value
    ) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return cb.like(cb.lower(expression), "%" + escaped + "%", '\\');
    }

    private static Boolean booleanValue(String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw AuditQuerySupport.badRequest(name + " must be true or false");
    }
}
