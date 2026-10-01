package com.owsm.AuthService.dto;

import java.util.List;

public record RoleMenuAccessResponse(String roleName, List<String> menuKeys) {
}
