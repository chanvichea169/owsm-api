package com.example.attendanceService.dto;

import jakarta.validation.constraints.NotBlank;

public record EmployeeLoginRequest(
    @NotBlank String identifier,
    @NotBlank String password
) {
}
