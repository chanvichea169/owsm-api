package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.dto.RoleRequest;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.service.RoleService;
import com.owsm.AuthService.service.handler.RoleServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleServiceHandler roleServiceHandler;
    @Override
    public RoleRequest createRole(RoleRequest roleRequest) {
        roleServiceHandler.validateRoleName(roleRequest.getName());
        Role role = new Role();
        role.setName(RoleName.valueOf(roleRequest.getName()));
        Role savedRole = roleRepository.save(role);
        return roleServiceHandler.convertToRoleRequest(savedRole);
    }

    @Override
    public RoleRequest getRoleById(Long id) {
        return null;
    }

    @Override
    public RoleRequest updateRole(Long id, RoleRequest roleRequest) {
        return null;
    }

    @Override
    public void deleteRole(Long id) {

    }

    @Override
    public RoleRequest getAllRoles() {
        return null;
    }
}
