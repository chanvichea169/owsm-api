package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.RoleRequest;

public interface RoleService {
        RoleRequest createRole(RoleRequest roleRequest);
        RoleRequest getRoleById(Long id);
        RoleRequest updateRole(Long id, RoleRequest roleRequest);
        void deleteRole(Long id);
        RoleRequest getAllRoles();

}
