package com.example.attendanceService.dto;

import com.example.attendanceService.model.Company;
import java.time.LocalDateTime;

public record CompanyResponse(
    Long id,
    String name,
    String code,
    String address,
    String phoneNumber,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getCode(),
            company.getAddress(),
            company.getPhoneNumber(),
            company.getEmail(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}
