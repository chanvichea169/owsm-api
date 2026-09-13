package com.owsm.AuthService.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserRequest {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Integer roleId;
}