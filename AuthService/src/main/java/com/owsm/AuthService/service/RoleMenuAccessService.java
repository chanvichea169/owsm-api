package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.RoleMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.model.RoleMenuConfiguration;
import com.owsm.AuthService.repository.RoleMenuConfigurationRepository;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleMenuAccessService {

    private static final Set<String> DEFAULT_MENU_KEYS = Set.of(
        "dashboard",
        "news-list",
        "categories",
        "media-videos",
        "audio",
        "attendance",
        "employee",
        "department",
        "offices",
        "attendance-report",
        "reports",
        "settings"
    );

    private final RoleMenuConfigurationRepository configurationRepository;
    private final SidebarMenuRepository menuRepository;

    public RoleMenuAccessService(
        RoleMenuConfigurationRepository configurationRepository,
        SidebarMenuRepository menuRepository
    ) {
        this.configurationRepository = configurationRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public RoleMenuAccessResponse getMenuAccess(RoleName roleName) {
        Set<String> menuKeys = configurationRepository.findByRoleName(roleName)
            .map(RoleMenuConfiguration::getMenuKeys)
            .orElseGet(() -> defaultMenuKeys(roleName));
        return response(roleName, menuKeys);
    }

    @Transactional
    public RoleMenuAccessResponse updateMenuAccess(
        RoleName roleName,
        RoleMenuAccessRequest request
    ) {
        Set<String> requestedKeys = new HashSet<>(request.menuKeys());
        Set<String> availableMenuKeys = menuRepository
            .findAllByEnabledTrueOrderBySortOrderAsc()
            .stream()
            .filter(menu -> menu.getPath() != null)
            .map(SidebarMenu::getMenuKey)
            .collect(java.util.stream.Collectors.toSet());
        if (!availableMenuKeys.containsAll(requestedKeys)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "One or more menu keys are invalid"
            );
        }
        if (roleName == RoleName.ADMIN && !requestedKeys.contains("settings")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The ADMIN role must retain access to Settings"
            );
        }
        RoleMenuConfiguration configuration = configurationRepository
            .findByRoleName(roleName)
            .orElseGet(() -> new RoleMenuConfiguration(roleName, Set.of()));
        configuration.setMenuKeys(requestedKeys);
        configurationRepository.save(configuration);
        return response(roleName, requestedKeys);
    }

    private Set<String> defaultMenuKeys(RoleName roleName) {
        Set<String> menuKeys = new HashSet<>(DEFAULT_MENU_KEYS);
        if (roleName == RoleName.ADMIN || roleName == RoleName.HEAD_OF_DEPARTMENT) {
            menuKeys.add("security-audit");
        }
        return menuKeys;
    }

    private RoleMenuAccessResponse response(
        RoleName roleName,
        Set<String> menuKeys
    ) {
        List<String> sortedKeys = menuKeys.stream().sorted().toList();
        return new RoleMenuAccessResponse(roleName.name(), sortedKeys);
    }
}
