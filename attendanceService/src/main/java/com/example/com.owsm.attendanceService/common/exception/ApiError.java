package com.example.attendanceService.common.exception;

import java.time.LocalDateTime;

public record ApiError(int status, String message, LocalDateTime timestamp) {
}
