package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.RoleActionGrantRequest;
import com.owsm.AuthService.dto.RoleActionGrantResponse;
import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.RoleMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.RoleMenuAccessService;
import com.owsm.AuthService.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/menu-access")
public class RoleMenuAccessController {

    private final RoleMenuAccessService roleMenuAccessService;
    private final RolePermissionService rolePermissionService;
    private final UserRepository userRepository;

    public RoleMenuAccessController(
        RoleMenuAccessService roleMenuAccessService,
        RolePermissionService rolePermissionService,
        UserRepository userRepository
    ) {
        this.roleMenuAccessService = roleMenuAccessService;
        this.rolePermissionService = rolePermissionService;
        this.userRepository = userRepository;
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

    /** Create / edit / delete permissions of the signed-in user's role. */
    @GetMapping("/me/actions")
    public RoleActionGrantResponse getMyActionPermissions(Authentication authentication) {
        return rolePermissionService.getRoleGrants(currentRole(authentication));
    }

    /** Create / edit / delete permissions configured for a role. */
    @GetMapping("/{roleName}/actions")
    public RoleActionGrantResponse getRoleActionPermissions(@PathVariable RoleName roleName) {
        return rolePermissionService.getRoleGrants(roleName);
    }

    @PutMapping("/{roleName}/actions")
    public RoleActionGrantResponse updateRoleActionPermissions(
        @PathVariable RoleName roleName,
        @Valid @RequestBody RoleActionGrantRequest request
    ) {
        return rolePermissionService.updateRoleGrants(roleName, request);
    }

    private RoleName currentRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(authentication.getName())
            .or(() -> userRepository.findByUsername(authentication.getName()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getRole() == null || user.getRole().getName() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The account has no role");
        }
        return user.getRole().getName();
    }
}

