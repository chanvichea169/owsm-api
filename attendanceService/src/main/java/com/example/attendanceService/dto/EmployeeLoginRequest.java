package com.example.attendanceService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeeLoginRequest(
    @Email @NotBlank String email,
    @NotBlank String password
) {
}
