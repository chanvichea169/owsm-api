package com.owsm.AuthService.securityaudit.service;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

public record ClientRequestInfo(
        InetAddress ipAddress,
        InetAddress forwardedIp,
        String userAgent,
        UUID deviceId,
        String deviceName,
        String deviceType,
        String browser,
        String browserVersion,
        String operatingSystem,
        String osVersion,
        String application,
        String platform,
        String apiVersion,
        Instant capturedAt) {
}
