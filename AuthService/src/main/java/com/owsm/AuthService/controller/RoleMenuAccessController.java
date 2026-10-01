package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.RoleMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.service.RoleMenuAccessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu-access")
public class RoleMenuAccessController {

    private final RoleMenuAccessService roleMenuAccessService;

    public RoleMenuAccessController(RoleMenuAccessService roleMenuAccessService) {
        this.roleMenuAccessService = roleMenuAccessService;
    }

    @GetMapping("/{roleName}")
    public RoleMenuAccessResponse getMenuAccess(@PathVariable RoleName roleName) {
        return roleMenuAccessService.getMenuAccess(roleName);
    }

    @PutMapping("/{roleName}")
    public RoleMenuAccessResponse updateMenuAccess(
        @PathVariable RoleName roleName,
        @Valid @RequestBody RoleMenuAccessRequest request
    ) {
        return roleMenuAccessService.updateMenuAccess(roleName, request);
    }
}
