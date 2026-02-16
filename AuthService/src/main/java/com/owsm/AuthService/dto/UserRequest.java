package com.owsm.AuthService.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private Long roleId;
}
