package com.example.attendanceService.dto;

import jakarta.validation.constraints.Email;

public record UpdateCompanyRequest(
    String name,
    String code,
    String address,
    String phoneNumber,
    @Email String email
) {
}
