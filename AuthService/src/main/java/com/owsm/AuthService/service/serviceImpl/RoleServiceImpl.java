package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.dto.RoleRequest;
import com.owsm.AuthService.dto.RoleResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.service.RoleService;
import com.owsm.AuthService.service.handler.RoleServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public RoleRequest geRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return roleServiceHandler.convertToRoleRequest(role);
    }
    @Override
    public RoleRequest updateRole(Long id, RoleRequest roleRequest) {
        roleServiceHandler.validateRoleName(roleRequest.getName());
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        role.setName(RoleName.valueOf(roleRequest.getName()));
        Role updatedRole = roleRepository.save(role);
        return roleServiceHandler.convertToRoleRequest(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        roleRepository.delete(role);
    }

    @Override
    public List<RoleResponse> getAllUsers() {
        return roleRepository.findAll()
                .stream()
                .map(roleServiceHandler::convertToRoleResponse)
                .collect(Collectors.toList());
    }
}
