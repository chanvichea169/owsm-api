package com.example.attendanceService.dto;

/** Result of a kiosk QR scan: who scanned and what was recorded. */
public record AttendanceScanResponse(
    EmployeeResponse employee,
    AttendanceRecordResponse record
) {
}
