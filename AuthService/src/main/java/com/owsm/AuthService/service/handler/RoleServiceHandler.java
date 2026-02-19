package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.RoleRequest;
import com.owsm.AuthService.dto.RoleResponse;
import com.owsm.AuthService.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoleServiceHandler {

    public void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.error("Role name is null or empty");
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        if (!name.matches("^[A-Z_]+$")) {
            log.error("Role name contains invalid characters: {}", name);
            throw new IllegalArgumentException("Role name must be uppercase letters and underscores only");
        }
    }

    public RoleRequest convertToRoleRequest(Role savedRole) {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(savedRole.getId());
        roleRequest.setName(savedRole.getName().name());
        return roleRequest;
    }

    public RoleResponse convertToRoleResponse(Role role) {
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(role.getId());
        roleResponse.setName(role.getName().name());
        return roleResponse;
    }
}
