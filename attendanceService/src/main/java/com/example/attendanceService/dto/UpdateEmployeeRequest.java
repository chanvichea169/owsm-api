package com.example.attendanceService.dto;

public record UpdateEmployeeRequest(
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    Long companyId,
    Long officeId,
    String password
) {
}
