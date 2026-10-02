package com.example.attendanceService.dto;

import com.example.attendanceService.model.AttendanceType;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload produced by a kiosk scanning an employee's QR badge. {@code type} is
 * optional: when omitted the service alternates check-in / check-out so a
 * single scan button works for the whole day.
 */
public record AttendanceScanRequest(
    @NotBlank String code,
    AttendanceType type,
    Double latitude,
    Double longitude,
    String notes
) {
}
