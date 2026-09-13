package com.owsm.AuthService.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String username;
    private String email;

    /** Email OTP verified */
    private boolean enabled;

    /** Admin-controlled — drives the UI toggle icon */
    private boolean active;

    /** Only populated on login / verify-otp */
    private String token;

    private RoleResponse role;
    private Date createdAt;
    private Date updatedAt;
}