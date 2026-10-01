package com.example.attendanceService.dto;

import com.example.attendanceService.model.AttendanceRecord;
import com.example.attendanceService.model.AttendanceType;
import java.time.LocalDateTime;

public record AttendanceRecordResponse(
    Long id,
    Long employeeId,
    String employeeName,
    String employeeNameEn,
    String employeeNameKh,
    Long companyId,
    String companyName,
    String companyNameEn,
    String companyNameKh,
    Long officeId,
    String officeName,
    String officeNameEn,
    String officeNameKh,
    AttendanceType type,
    LocalDateTime recordedAt,
    Double latitude,
    Double longitude,
    String notes,
    String notesEn,
    String notesKh
) {

    public static AttendanceRecordResponse from(AttendanceRecord record) {
        var employee = record.getEmployee();
        var company = employee.getCompany();
        var office = employee.getOffice();
        return new AttendanceRecordResponse(
            record.getId(),
            employee.getId(),
            localizedName(employee.getFirstName(), employee.getLastName()),
            localizedName(employee.getFirstNameEn(), employee.getLastNameEn()),
            localizedName(employee.getFirstNameKh(), employee.getLastNameKh()),
            company.getId(),
            company.getName(),
            company.getNameEn(),
            company.getNameKh(),
            office != null ? office.getId() : null,
            office != null ? office.getName() : null,
            office != null ? office.getNameEn() : null,
            office != null ? office.getNameKh() : null,
            record.getType(),
            record.getRecordedAt(),
            record.getLatitude(),
            record.getLongitude(),
            record.getNotes(),
            record.getNotesEn(),
            record.getNotesKh()
        );
    }

    private static String localizedName(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            return lastName == null ? "" : lastName;
        }
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
