package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserMenuAccessResponse;
import com.owsm.AuthService.dto.RoleMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.UserMenuConfiguration;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import com.owsm.AuthService.repository.UserMenuConfigurationRepository;
import com.owsm.AuthService.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserMenuAccessService {

    private final UserMenuConfigurationRepository configurationRepository;
    private final SidebarMenuRepository menuRepository;
    private final UserRepository userRepository;
    private final RoleMenuAccessService roleMenuAccessService;

    public UserMenuAccessService(
        UserMenuConfigurationRepository configurationRepository,
        SidebarMenuRepository menuRepository,
        UserRepository userRepository,
        RoleMenuAccessService roleMenuAccessService
    ) {
        this.configurationRepository = configurationRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.roleMenuAccessService = roleMenuAccessService;
    }

    @Transactional
    public UserMenuAccessResponse getMenuAccess(Long userId) {
        User user = findUser(userId);
        var configuration = configurationRepository.findByUserId(userId);
        Set<String> menuKeys = configuration
            .map(UserMenuConfiguration::getMenuKeys)
            .orElseGet(() -> inheritedRoleMenuKeys(user));
        return response(userId, menuKeys, configuration.isPresent());
    }

    @Transactional
    public UserMenuAccessResponse updateMenuAccess(
        Long userId,
        List<String> requestedMenuKeys
    ) {
        User user = findUser(userId);
        Set<String> menuKeys = new HashSet<>(requestedMenuKeys);
        Set<String> availableMenuKeys = menuRepository
            .findAllByEnabledTrueOrderBySortOrderAsc()
            .stream()
            .filter(menu -> menu.getPath() != null)
            .map(SidebarMenu::getMenuKey)
            .collect(java.util.stream.Collectors.toSet());
        if (!availableMenuKeys.containsAll(menuKeys)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "One or more menu keys are invalid"
            );
        }
        if (
            user.getRole() != null &&
            user.getRole().getName() == RoleName.ADMIN &&
            !menuKeys.contains("settings")
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The ADMIN user must retain access to Settings"
            );
        }

        UserMenuConfiguration configuration = configurationRepository
            .findByUserId(userId)
            .orElseGet(() -> new UserMenuConfiguration(userId, Set.of()));
        configuration.setMenuKeys(menuKeys);
        configurationRepository.save(configuration);
        return response(userId, menuKeys, true);
    }

    @Transactional
    public UserMenuAccessResponse resetMenuAccess(Long userId) {
        findUser(userId);
        configurationRepository.deleteByUserId(userId);
        return getMenuAccess(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));
    }

    private Set<String> inheritedRoleMenuKeys(User user) {
        if (user.getRole() == null || user.getRole().getName() == null) {
            return Set.of();
        }
        RoleMenuAccessResponse roleAccess = roleMenuAccessService.getMenuAccess(
            user.getRole().getName()
        );
        return new HashSet<>(roleAccess.menuKeys());
    }

    private UserMenuAccessResponse response(
        Long userId,
        Set<String> menuKeys,
        boolean custom
    ) {
        return new UserMenuAccessResponse(
            userId,
            menuKeys.stream().sorted().toList(),
            custom
        );
    }
}
