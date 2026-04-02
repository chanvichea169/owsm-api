package com.example.attendanceService.dto;

import com.example.attendanceService.model.AttendanceRecord;
import com.example.attendanceService.model.AttendanceType;
import java.time.LocalDateTime;

public record AttendanceRecordResponse(
    Long id,
    Long employeeId,
    String employeeName,
    AttendanceType type,
    LocalDateTime recordedAt,
    Double latitude,
    Double longitude,
    String notes
) {

    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
            record.getId(),
            record.getEmployee().getId(),
            record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName(),
            record.getType(),
            record.getRecordedAt(),
            record.getLatitude(),
            record.getLongitude(),
            record.getNotes()
        );
    }
}
