package com.owsm.AuthService.securityaudit.dto;

import com.owsm.AuthService.securityaudit.entity.LoginAudit;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;

import java.time.Instant;

public record LoginAuditResponse(
        Long id,
        Long userId,
        String username,
        String email,
        String ipAddress,
        Instant occurredAt,
        String userAgent,
        LoginStatus status,
        boolean suspicious,
        Integer riskScore,
        String authMethod,
        String deviceName,
        String deviceType,
        String application
) {
    public static LoginAuditResponse from(LoginAudit audit) {
        var user = audit.getUser();
        return new LoginAuditResponse(
                audit.getId(),
                user == null ? null : user.getId(),
                audit.getUsername() != null ? audit.getUsername() : user == null ? null : user.getUsername(),
                audit.getEmail() != null ? audit.getEmail() : user == null ? null : user.getEmail(),
                audit.getIpAddress() == null ? null : audit.getIpAddress().getHostAddress(),
                audit.getOccurredAt(),
                audit.getUserAgent(),
                audit.getStatus(),
                audit.isSuspicious(),
                audit.getRiskScore(),
                audit.getAuthMethod(),
                audit.getDeviceName(),
                audit.getDeviceType(),
                audit.getApplication()
        );
    }
}
