package com.owsm.AuthService.dto;

import lombok.Data;

@Data
public class RoleRequest {
    private Long id;
    private String name;
    private String description;
}
