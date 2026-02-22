package com.owsm.AuthService.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private String token;
    private RoleResponse role;
    private Date createdAt;
    private Date updatedAt;
}
