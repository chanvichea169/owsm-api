package com.owsm.AuthService.securityaudit.service;

import com.owsm.AuthService.model.User;
import com.owsm.AuthService.securityaudit.entity.AuthDevice;
import com.owsm.AuthService.securityaudit.entity.LoginAudit;
import com.owsm.AuthService.securityaudit.entity.SecurityAudit;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventType;
import com.owsm.AuthService.securityaudit.repository.AuthDeviceRepository;
import com.owsm.AuthService.securityaudit.repository.LoginAuditRepository;
import com.owsm.AuthService.securityaudit.repository.SecurityAuditRepository;
import com.owsm.AuthService.service.TelegramAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationAuditService {
    private final LoginAuditRepository loginAuditRepository;
    private final SecurityAuditRepository securityAuditRepository;
    private final AuthDeviceRepository authDeviceRepository;
    private final ClientRequestInfoExtractor requestInfoExtractor;
    private final LoginRiskAssessmentService riskAssessmentService;
    private final TelegramAlertService telegramAlertService;

    public AuthenticationAuditService(
            LoginAuditRepository loginAuditRepository,
            SecurityAuditRepository securityAuditRepository,
            AuthDeviceRepository authDeviceRepository,
            ClientRequestInfoExtractor requestInfoExtractor,
            LoginRiskAssessmentService riskAssessmentService,
            TelegramAlertService telegramAlertService) {
        this.loginAuditRepository = loginAuditRepository;
        this.securityAuditRepository = securityAuditRepository;
        this.authDeviceRepository = authDeviceRepository;
        this.requestInfoExtractor = requestInfoExtractor;
        this.riskAssessmentService = riskAssessmentService;
        this.telegramAlertService = telegramAlertService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginAttempt(
            User user,
            String identifier,
            LoginStatus status,
            String failureReason,
            String authenticationMethod,
            UUID sessionId,
            UUID tokenId) {
        recordLoginAttempt(
                user, identifier, status, failureReason, authenticationMethod, sessionId, tokenId, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginAttempt(
            User user,
            String identifier,
            LoginStatus status,
            String failureReason,
            String authenticationMethod,
            UUID sessionId,
            UUID tokenId,
            Instant sessionExpiresAt) {
        ClientRequestInfo info = requestInfoExtractor.currentRequest();
        AuthDevice device = null;
        boolean newDevice = false;
        if (user != null && info.deviceId() != null) {
            var existing = authDeviceRepository.findByUser_IdAndDeviceId(user.getId(), info.deviceId());
            newDevice = existing.isEmpty();
            if (status == LoginStatus.SUCCESS) {
                device = existing.orElseGet(() -> AuthDevice.builder()
                        .user(user)
                        .deviceId(info.deviceId())
                        .build());
                device.setDisplayName(info.deviceName());
                device.setDeviceType(info.deviceType());
                device.setPlatform(info.platform());
                device.setUserAgent(info.userAgent());
                device.setLastIpAddress(info.ipAddress());
                device.setLastSeenAt(info.capturedAt());
                device = authDeviceRepository.save(device);
            } else {
                device = existing.orElse(null);
            }
        }

        String normalizedIdentifier = identifier == null ? null : identifier.trim();
        LoginRiskAssessmentService.RiskAssessment risk =
                riskAssessmentService.assess(user, normalizedIdentifier, info, newDevice);
        String email = user != null
                ? user.getEmail()
                : normalizedIdentifier != null && normalizedIdentifier.contains("@")
                        ? normalizedIdentifier
                        : null;

        LoginAudit audit = new LoginAudit();
        audit.setUser(user);
        audit.setUsername(user != null ? user.getUsername() : email == null ? normalizedIdentifier : null);
        audit.setEmail(email);
        audit.setIpAddress(info.ipAddress());
        audit.setForwardedIp(info.forwardedIp());
        audit.setUserAgent(info.userAgent());
        audit.setStatus(status);
        audit.setFailureReason(failureReason);
        audit.setAuthMethod(authenticationMethod);
        audit.setSessionId(sessionId);
        audit.setTokenId(tokenId);
        audit.setDeviceId(info.deviceId() == null ? null : info.deviceId().toString());
        audit.setDeviceName(info.deviceName());
        audit.setDeviceType(info.deviceType());
        audit.setBrowser(info.browser());
        audit.setBrowserVersion(info.browserVersion());
        audit.setOperatingSystem(info.operatingSystem());
        audit.setOsVersion(info.osVersion());
        audit.setApplication(info.application());
        audit.setPlatform(info.platform());
        audit.setApiVersion(info.apiVersion());
        audit.setSuspicious(risk.suspicious());
        audit.setRiskScore(risk.score());
        audit.setMfaRequired(
                status == LoginStatus.MFA_REQUIRED
                        || status == LoginStatus.MFA_FAILED
                        || status == LoginStatus.SUCCESS);
        audit.setMfaSuccessful(status == LoginStatus.SUCCESS ? Boolean.TRUE
                : status == LoginStatus.MFA_FAILED ? Boolean.FALSE : null);
        audit.setOccurredAt(info.capturedAt());
        audit.setSessionExpiresAt(sessionExpiresAt);
        if (status == LoginStatus.SUCCESS) {
            audit.setLoginAt(info.capturedAt());
            audit.setMfaMethod("EMAIL_OTP");
            audit.setMfaAt(info.capturedAt());
        }
        loginAuditRepository.save(audit);

        SecurityEventType eventType = eventFor(status);
        SecurityAudit event = new SecurityAudit();
        event.setUser(user);
        event.setEventType(eventType);
        event.setStatus(status == LoginStatus.SUCCESS || status == LoginStatus.MFA_REQUIRED
                ? SecurityEventStatus.SUCCESS
                : SecurityEventStatus.FAILURE);
        event.setOccurredAt(info.capturedAt());
        event.setDescription(failureReason);
        event.setIpAddress(info.ipAddress());
        event.setForwardedIp(info.forwardedIp());
        event.setUserAgent(info.userAgent());
        event.setDevice(device);
        event.setMetadata(requestMetadata(info, Map.of(
                "loginStatus", status.name(),
                "riskScore", risk.score(),
                "suspicious", risk.suspicious())));
        securityAuditRepository.save(event);

        if (newDevice) {
            SecurityAudit newDeviceEvent = new SecurityAudit();
            newDeviceEvent.setUser(user);
            newDeviceEvent.setEventType(SecurityEventType.NEW_DEVICE);
            newDeviceEvent.setStatus(SecurityEventStatus.DETECTED);
            newDeviceEvent.setOccurredAt(info.capturedAt());
            newDeviceEvent.setDescription("Authentication from a previously unseen device");
            newDeviceEvent.setIpAddress(info.ipAddress());
            newDeviceEvent.setForwardedIp(info.forwardedIp());
            newDeviceEvent.setUserAgent(info.userAgent());
            newDeviceEvent.setDevice(device);
            newDeviceEvent.setMetadata(requestMetadata(
                    info, Map.of("deviceType", info.deviceType() == null ? "unknown" : info.deviceType())));
            securityAuditRepository.save(newDeviceEvent);
            telegramAlertService.securityEvent(
                    "New device sign-in",
                    "user: " + describe(user, identifier)
                            + "\nip: " + asText(info.ipAddress())
                            + "\ndevice: " + asText(info.deviceType()),
                    describe(user, identifier));
        }

        if (risk.suspicious()) {
            SecurityAudit suspiciousEvent = new SecurityAudit();
            suspiciousEvent.setUser(user);
            suspiciousEvent.setEventType(SecurityEventType.SUSPICIOUS_ACTIVITY);
            suspiciousEvent.setStatus(SecurityEventStatus.DETECTED);
            suspiciousEvent.setOccurredAt(info.capturedAt());
            suspiciousEvent.setDescription("Login attempt met a configured risk detection rule");
            suspiciousEvent.setIpAddress(info.ipAddress());
            suspiciousEvent.setForwardedIp(info.forwardedIp());
            suspiciousEvent.setUserAgent(info.userAgent());
            suspiciousEvent.setDevice(device);
            suspiciousEvent.setMetadata(requestMetadata(info, Map.of("riskScore", risk.score())));
            securityAuditRepository.save(suspiciousEvent);
            telegramAlertService.securityEvent(
                    "Suspicious login detected",
                    "user: " + describe(user, identifier)
                            + "\nip: " + asText(info.ipAddress())
                            + "\nrisk score: " + risk.score() + "/100"
                            + "\nreason: " + (failureReason == null ? "risk detection rule" : failureReason),
                    describe(user, identifier));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogout(User user, UUID sessionId) {
        ClientRequestInfo info = requestInfoExtractor.currentRequest();
        Instant now = info.capturedAt();
        if (sessionId != null) {
            loginAuditRepository
                    .findFirstBySessionIdAndStatusOrderByOccurredAtDesc(sessionId, LoginStatus.SUCCESS)
                    .ifPresent(loginAudit -> {
                        loginAudit.setLogoutAt(now);
                        loginAuditRepository.save(loginAudit);
                    });
        }
        SecurityAudit event = new SecurityAudit();
        event.setUser(user);
        event.setEventType(SecurityEventType.LOGOUT);
        event.setStatus(SecurityEventStatus.SUCCESS);
        event.setOccurredAt(now);
        event.setIpAddress(info.ipAddress());
        event.setForwardedIp(info.forwardedIp());
        event.setUserAgent(info.userAgent());
        event.setMetadata(requestMetadata(
                info, sessionId == null ? Map.of() : Map.of("sessionId", sessionId.toString())));
        securityAuditRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSecurityEvent(
            User user,
            SecurityEventType eventType,
            SecurityEventStatus status,
            String description,
            Map<String, Object> metadata) {
        ClientRequestInfo info = requestInfoExtractor.currentRequest();
        SecurityAudit event = new SecurityAudit();
        event.setUser(user);
        event.setEventType(eventType);
        event.setStatus(status);
        event.setOccurredAt(info.capturedAt());
        event.setDescription(description);
        event.setIpAddress(info.ipAddress());
        event.setForwardedIp(info.forwardedIp());
        event.setUserAgent(info.userAgent());
        event.setMetadata(requestMetadata(info, metadata == null ? Map.of() : metadata));
        securityAuditRepository.save(event);

        if (status == SecurityEventStatus.DENIED || status == SecurityEventStatus.DETECTED) {
            telegramAlertService.securityEvent(
                    "Security event: " + eventType.name(),
                    "user: " + describe(user, null)
                            + "\nstatus: " + status.name()
                            + "\nip: " + asText(info.ipAddress())
                            + (description == null || description.isBlank() ? "" : "\nreason: " + description),
                    user == null ? "system" : describe(user, null));
        }
    }

    private static String describe(User user, String identifier) {
        if (user != null) {
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                return user.getUsername();
            }
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                return user.getEmail();
            }
        }
        return identifier == null || identifier.isBlank() ? "unknown" : identifier;
    }

    private static String asText(Object value) {
        return value == null ? "unknown" : String.valueOf(value);
    }

    private Map<String, Object> requestMetadata(ClientRequestInfo info, Map<String, ?> metadata) {
        Map<String, Object> enriched = new java.util.HashMap<>(metadata);
        if (info.application() != null) {
            enriched.put("application", info.application());
        }
        if (info.platform() != null) {
            enriched.put("platform", info.platform());
        }
        return Map.copyOf(enriched);
    }

    private SecurityEventType eventFor(LoginStatus status) {
        return switch (status) {
            case SUCCESS -> SecurityEventType.LOGIN_SUCCESS;
            case FAILED -> SecurityEventType.LOGIN_FAILED;
            case LOCKED -> SecurityEventType.LOGIN_LOCKED;
            case MFA_REQUIRED -> SecurityEventType.LOGIN_MFA_REQUIRED;
            case MFA_FAILED -> SecurityEventType.LOGIN_MFA_FAILED;
            case TOKEN_EXPIRED -> SecurityEventType.LOGIN_TOKEN_EXPIRED;
            case ACCOUNT_DISABLED -> SecurityEventType.LOGIN_ACCOUNT_DISABLED;
            case ACCOUNT_NOT_FOUND -> SecurityEventType.LOGIN_ACCOUNT_NOT_FOUND;
        };
    }
}
