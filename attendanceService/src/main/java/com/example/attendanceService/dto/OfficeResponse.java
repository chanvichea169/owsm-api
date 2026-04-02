package com.example.attendanceService.dto;

import com.example.attendanceService.model.Office;
import java.time.LocalDateTime;

public record OfficeResponse(
    Long id,
    String name,
    String code,
    String address,
    String phoneNumber,
    String email,
    Long companyId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static OfficeResponse from(Office office) {
        return new OfficeResponse(
            office.getId(),
            office.getName(),
            office.getCode(),
            office.getAddress(),
            office.getPhoneNumber(),
            office.getEmail(),
            office.getCompany().getId(),
            office.getCreatedAt(),
            office.getUpdatedAt()
        );
    }
}
