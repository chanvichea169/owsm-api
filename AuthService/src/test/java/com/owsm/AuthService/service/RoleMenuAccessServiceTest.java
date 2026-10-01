package com.owsm.AuthService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.RoleMenuConfiguration;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.repository.RoleMenuConfigurationRepository;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RoleMenuAccessServiceTest {

    @Mock
    private RoleMenuConfigurationRepository configurationRepository;

    @Mock
    private SidebarMenuRepository menuRepository;

    @InjectMocks
    private RoleMenuAccessService service;

    private void configureAvailableMenus() {
        when(menuRepository.findAllByEnabledTrueOrderBySortOrderAsc())
            .thenReturn(List.of(
                new SidebarMenu(
                    "dashboard",
                    "app.nav.dashboard",
                    null,
                    "/",
                    "dashboard",
                    null,
                    0,
                    true
                ),
                new SidebarMenu(
                    "attendance",
                    "app.nav.attendance",
                    null,
                    "/attendance",
                    "calendar",
                    null,
                    1,
                    true
                ),
                new SidebarMenu(
                    "settings",
                    "app.nav.settings",
                    null,
                    "/settings",
                    "settings",
                    null,
                    2,
                    true
                )
            ));
    }

    @Test
    void returnsDefaultMenuAccessForUnconfiguredRoles() {
        when(configurationRepository.findByRoleName(RoleName.USER))
            .thenReturn(Optional.empty());
        when(configurationRepository.findByRoleName(RoleName.ADMIN))
            .thenReturn(Optional.empty());

        var userAccess = service.getMenuAccess(RoleName.USER);
        var adminAccess = service.getMenuAccess(RoleName.ADMIN);

        assertTrue(userAccess.menuKeys().contains("dashboard"));
        assertFalse(userAccess.menuKeys().contains("security-audit"));
        assertTrue(adminAccess.menuKeys().contains("security-audit"));
    }

    @Test
    void savesAndReturnsSelectedMenuKeys() {
        configureAvailableMenus();
        when(configurationRepository.findByRoleName(RoleName.USER))
            .thenReturn(Optional.empty());
        when(configurationRepository.save(any(RoleMenuConfiguration.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateMenuAccess(
            RoleName.USER,
            new RoleMenuAccessRequest(List.of("dashboard", "attendance"))
        );
        ArgumentCaptor<RoleMenuConfiguration> savedConfiguration =
            ArgumentCaptor.forClass(RoleMenuConfiguration.class);
        verify(configurationRepository).save(savedConfiguration.capture());

        assertEquals(List.of("attendance", "dashboard"), result.menuKeys());
        assertEquals(
            Set.of("dashboard", "attendance"),
            savedConfiguration.getValue().getMenuKeys()
        );
    }

    @Test
    void acceptsKeysFromDynamicallyCreatedMenus() {
        when(menuRepository.findAllByEnabledTrueOrderBySortOrderAsc())
            .thenReturn(List.of(
                new SidebarMenu(
                    "quick-links",
                    "Quick links",
                    null,
                    "/reports",
                    "list",
                    null,
                    3,
                    true
                )
            ));
        when(configurationRepository.findByRoleName(RoleName.USER))
            .thenReturn(Optional.empty());
        when(configurationRepository.save(any(RoleMenuConfiguration.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateMenuAccess(
            RoleName.USER,
            new RoleMenuAccessRequest(List.of("quick-links"))
        );

        assertEquals(List.of("quick-links"), result.menuKeys());
    }

    @Test
    void returnsSavedMenuAccessInsteadOfDefaults() {
        var configuration = new RoleMenuConfiguration(
            RoleName.USER,
            Set.of("attendance")
        );
        when(configurationRepository.findByRoleName(RoleName.USER))
            .thenReturn(Optional.of(configuration));

        var result = service.getMenuAccess(RoleName.USER);

        assertEquals(List.of("attendance"), result.menuKeys());
    }

    @Test
    void rejectsUnknownMenuKeys() {
        configureAvailableMenus();
        assertThrows(
            ResponseStatusException.class,
            () -> service.updateMenuAccess(
                RoleName.USER,
                new RoleMenuAccessRequest(List.of("not-a-sidebar-item"))
            )
        );
    }

    @Test
    void rejectsMenuGroupsAsRoleAccessKeys() {
        when(menuRepository.findAllByEnabledTrueOrderBySortOrderAsc())
            .thenReturn(List.of(
                new SidebarMenu(
                    "news-management",
                    "app.nav.news-management",
                    null,
                    null,
                    "news",
                    null,
                    0,
                    true
                )
            ));

        assertThrows(
            ResponseStatusException.class,
            () -> service.updateMenuAccess(
                RoleName.USER,
                new RoleMenuAccessRequest(List.of("news-management"))
            )
        );
    }

    @Test
    void preventsRemovingAdminSettingsAccess() {
        configureAvailableMenus();
        assertThrows(
            ResponseStatusException.class,
            () -> service.updateMenuAccess(
                RoleName.ADMIN,
                new RoleMenuAccessRequest(List.of("dashboard"))
            )
        );
    }
}
