package com.example.attendanceService.dto;

import com.example.attendanceService.model.AttendanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.OffsetDateTime;

public record AttendanceEventRequest(
    @NotNull Long employeeId,
    @NotNull AttendanceType type,
    @PastOrPresent OffsetDateTime recordedAt,
    Double latitude,
    Double longitude,
    String notes
) {
}
