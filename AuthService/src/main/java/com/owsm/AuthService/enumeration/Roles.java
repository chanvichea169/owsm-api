package com.owsm.AuthService.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Roles {
    ADMIN, HR, OFFICER, OWNER_COMPANY, USER;
    @JsonCreator
    public static Roles fromString(String role) {
        return Roles.valueOf(role.toUpperCase());
    }
}
