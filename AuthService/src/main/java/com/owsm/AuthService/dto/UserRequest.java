package com.owsm.AuthService.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private Long roleId;
    private Date createdAt;
    private Date updatedAt;
}
