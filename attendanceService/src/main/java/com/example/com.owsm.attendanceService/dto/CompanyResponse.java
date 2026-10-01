package com.example.attendanceService.dto;

import com.example.attendanceService.model.Company;
import java.time.LocalDateTime;

public record CompanyResponse(
    Long id,
    String name,
    String nameEn,
    String nameKh,
    String code,
    String address,
    String phoneNumber,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements java.io.Serializable {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getNameEn(),
            company.getNameKh(),
            company.getCode(),
            company.getAddress(),
            company.getPhoneNumber(),
            company.getEmail(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}
