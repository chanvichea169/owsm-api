package com.owsm.AuthService.securityaudit.service;

import com.owsm.AuthService.securityaudit.entity.LoginAudit;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;
import com.owsm.AuthService.securityaudit.repository.LoginAuditRepository;
import com.owsm.AuthService.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoginRiskAssessmentService {
    private final LoginAuditRepository loginAuditRepository;
    private final int suspiciousFailureThreshold;
    private final int failureLookbackMinutes;

    public LoginRiskAssessmentService(
            LoginAuditRepository loginAuditRepository,
            @Value("${security.audit.suspicious-failure-threshold:5}") int suspiciousFailureThreshold,
            @Value("${security.audit.failure-lookback-minutes:15}") int failureLookbackMinutes) {
        this.loginAuditRepository = loginAuditRepository;
        this.suspiciousFailureThreshold = suspiciousFailureThreshold;
        this.failureLookbackMinutes = failureLookbackMinutes;
    }

    public RiskAssessment assess(User user, String identifier, ClientRequestInfo requestInfo, boolean newDevice) {
        Instant since = Instant.now().minus(failureLookbackMinutes, ChronoUnit.MINUTES);
        List<String> normalizedIdentifiers = new ArrayList<>();
        if (identifier != null && !identifier.isBlank()) {
            normalizedIdentifiers.add(identifier.trim().toLowerCase());
        }
        if (user != null) {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                normalizedIdentifiers.add(user.getEmail().trim().toLowerCase());
            }
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                normalizedIdentifiers.add(user.getUsername().trim().toLowerCase());
            }
        }
        if (requestInfo.ipAddress() == null && normalizedIdentifiers.isEmpty()) {
            return new RiskAssessment(newDevice ? 25 : 0, newDevice);
        }

        Specification<LoginAudit> recentFailures = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> matchingOrigin = new ArrayList<>();
            if (requestInfo.ipAddress() != null) {
                matchingOrigin.add(builder.equal(root.get("ipAddress"), requestInfo.ipAddress()));
            }
            for (String normalizedIdentifier : normalizedIdentifiers) {
                matchingOrigin.add(builder.or(
                        builder.equal(builder.lower(root.get("email")), normalizedIdentifier),
                        builder.equal(builder.lower(root.get("username")), normalizedIdentifier)));
            }
            return builder.and(
                    root.get("status").in(LoginStatus.FAILED, LoginStatus.MFA_FAILED),
                    builder.greaterThanOrEqualTo(root.get("occurredAt"), since),
                    builder.or(matchingOrigin.toArray(jakarta.persistence.criteria.Predicate[]::new)));
        };

        long failureCount = loginAuditRepository.count(recentFailures);
        int score = (int) Math.min(100, failureCount * 15 + (newDevice ? 25 : 0));
        return new RiskAssessment(
                score,
                failureCount >= suspiciousFailureThreshold || newDevice);
    }

    public record RiskAssessment(int score, boolean suspicious) {
    }
}
