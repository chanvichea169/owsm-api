package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.MenuActionGrant;
import com.owsm.AuthService.dto.RoleActionGrantRequest;
import com.owsm.AuthService.dto.RoleActionGrantResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.RoleActionPermission;
import com.owsm.AuthService.model.RoleMenuConfiguration;
import com.owsm.AuthService.repository.RoleActionPermissionRepository;
import com.owsm.AuthService.repository.RoleMenuConfigurationRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Stores and retrieves the create / edit / delete permissions a role holds on a
 * page (menu). View access stays in {@link RoleMenuConfiguration}.
 */
@Service
public class RolePermissionService {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_EDIT = "EDIT";
    public static final String ACTION_DELETE = "DELETE";

    private static final Set<String> ALLOWED_ACTIONS =
        Set.of(ACTION_CREATE, ACTION_EDIT, ACTION_DELETE);

    private final RoleActionPermissionRepository permissionRepository;
    private final RoleMenuConfigurationRepository menuRepository;

    public RolePermissionService(
        RoleActionPermissionRepository permissionRepository,
        RoleMenuConfigurationRepository menuRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public RoleActionGrantResponse getRoleGrants(RoleName roleName) {
        return toResponse(roleName, permissionRepository.findByRoleName(roleName));
    }

    @Transactional
    public RoleActionGrantResponse updateRoleGrants(
        RoleName roleName,
        RoleActionGrantRequest request
    ) {
        Set<String> viewKeys = menuRepository.findByRoleName(roleName)
            .map(RoleMenuConfiguration::getMenuKeys)
            .orElseGet(Set::of);

        List<RoleActionPermission> replacements = new ArrayList<>();
        if (request != null && request.grants() != null) {
            for (MenuActionGrant grant : request.grants()) {
                if (grant == null || grant.menuKey() == null || grant.actions() == null) {
                    continue;
                }
                String menuKey = grant.menuKey().trim();
                /* A role can only act on a page it is allowed to view. */
                if (menuKey.isEmpty() || !viewKeys.contains(menuKey)) {
                    continue;
                }
                for (String rawAction : grant.actions()) {
                    String action = rawAction == null ? "" : rawAction.trim().toUpperCase();
                    if (ALLOWED_ACTIONS.contains(action)) {
                        replacements.add(new RoleActionPermission(roleName, menuKey, action));
                    }
                }
            }
        }

        permissionRepository.deleteAllByRoleName(roleName);
        permissionRepository.saveAll(replacements);
        return toResponse(roleName, replacements);
    }

    private RoleActionGrantResponse toResponse(
        RoleName roleName,
        List<RoleActionPermission> permissions
    ) {
        Map<String, Set<String>> grouped = new LinkedHashMap<>();
        for (RoleActionPermission permission : permissions) {
            grouped
                .computeIfAbsent(permission.getMenuKey(), key -> new LinkedHashSet<>())
                .add(permission.getAction());
        }
        List<MenuActionGrant> grants = grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new MenuActionGrant(
                entry.getKey(),
                entry.getValue().stream().sorted().toList()
            ))
            .toList();
        return new RoleActionGrantResponse(roleName.name(), grants);
    }
}
