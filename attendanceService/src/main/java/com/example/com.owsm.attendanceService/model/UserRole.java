package com.example.attendanceService.model;

import java.util.Arrays;
import java.util.Locale;

public enum UserRole {
    ADMIN,
    HR,
    HEAD_OF_DEPARTMENT,
    EMPLOYEE,
    MANAGER;

    public static UserRole from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(role -> role.name().equals(normalized))
            .findFirst()
            .orElse(null);
    }
}
