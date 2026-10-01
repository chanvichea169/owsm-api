package com.owsm.AuthService.securityaudit.dto;

import com.owsm.AuthService.securityaudit.entity.SecurityAudit;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventType;

import java.time.Instant;

public record SecurityEventResponse(
        Long id,
        Long userId,
        String username,
        String email,
        SecurityEventType eventType,
        SecurityEventStatus status,
        Instant occurredAt,
        String ipAddress,
        String userAgent,
        String deviceName,
        String deviceType,
        String application,
        boolean suspicious
) {
    public static SecurityEventResponse from(SecurityAudit event) {
        var user = event.getUser();
        var device = event.getDevice();
        var metadata = event.getMetadata();
        return new SecurityEventResponse(
                event.getId(),
                user == null ? null : user.getId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                event.getEventType(),
                event.getStatus(),
                event.getOccurredAt(),
                event.getIpAddress() == null ? null : event.getIpAddress().getHostAddress(),
                event.getUserAgent(),
                device == null ? null : device.getDisplayName(),
                device == null ? null : device.getDeviceType(),
                metadata == null || !(metadata.get("application") instanceof String application)
                        ? null : application,
                event.getStatus() == SecurityEventStatus.DETECTED
                        || metadata != null && (Boolean.TRUE.equals(metadata.get("suspicious"))
                        || "true".equalsIgnoreCase(String.valueOf(metadata.get("suspicious"))))
        );
    }
}
