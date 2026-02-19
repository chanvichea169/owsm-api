package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.RoleRequest;
import com.owsm.AuthService.dto.RoleResponse;

import java.util.List;

public interface RoleService {
        RoleRequest createRole(RoleRequest roleRequest);
        RoleRequest geRoleById(Long id);
        RoleRequest updateRole(Long id, RoleRequest roleRequest);
        void deleteRole(Long id);
        List<RoleResponse> getAllUsers();
}
