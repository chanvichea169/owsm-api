package com.owsm.AuthService.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        Instant createdAt,
        boolean read
) {
}