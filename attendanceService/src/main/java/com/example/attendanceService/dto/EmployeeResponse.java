package com.example.attendanceService.dto;

import com.example.attendanceService.model.Employee;
import java.time.LocalDateTime;

public record EmployeeResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    Long companyId,
    Long officeId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getPhoneNumber(),
            employee.getCompany().getId(),
            employee.getOffice() != null ? employee.getOffice().getId() : null,
            employee.getCreatedAt(),
            employee.getUpdatedAt()
        );
    }
}
