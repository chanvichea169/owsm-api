package com.owsm.AuthService.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Roles {
    ADMIN, DOCTOR, PATIENT;
    @JsonCreator
    public static Roles fromString(String role) {
        return Roles.valueOf(role.toUpperCase());
    }
}
