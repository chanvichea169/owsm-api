package com.example.attendanceService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOfficeRequest(
    @NotBlank String name,
    @NotBlank String code,
    String address,
    String phoneNumber,
    @Email String email
) {
}
