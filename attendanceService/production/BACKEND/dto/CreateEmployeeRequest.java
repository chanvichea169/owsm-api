package com.example.attendanceService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String username,
    @Email String email,
    String phoneNumber,
    @NotNull Long companyId,
    Long officeId,
    @NotBlank String password
) {
}
