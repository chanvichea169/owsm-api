package com.owsm.AuthService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.owsm.AuthService.dto.RoleMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.UserMenuConfiguration;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import com.owsm.AuthService.repository.UserMenuConfigurationRepository;
import com.owsm.AuthService.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMenuAccessServiceTest {

    @Mock
    private UserMenuConfigurationRepository configurationRepository;

    @Mock
    private SidebarMenuRepository menuRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMenuAccessService roleMenuAccessService;

    @InjectMocks
    private UserMenuAccessService service;

    @Test
    void usesRoleMenuAccessAsDefaultUntilUserOverridesIt() {
        User user = userWithRole(RoleName.USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(configurationRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(roleMenuAccessService.getMenuAccess(RoleName.USER))
            .thenReturn(new RoleMenuAccessResponse("USER", List.of("dashboard")));

        var response = service.getMenuAccess(7L);

        assertEquals(7L, response.userId());
        assertEquals(List.of("dashboard"), response.menuKeys());
        assertEquals(false, response.custom());
    }

    @Test
    void usesSavedUserMenuSelectionInsteadOfRoleDefault() {
        User user = userWithRole(RoleName.USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(configurationRepository.findByUserId(7L))
            .thenReturn(Optional.of(new UserMenuConfiguration(7L, Set.of("custom-report"))));

        var response = service.getMenuAccess(7L);

        assertEquals(List.of("custom-report"), response.menuKeys());
        assertEquals(true, response.custom());
    }

    @Test
    void savesAnExplicitUserMenuOverride() {
        User user = userWithRole(RoleName.USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(menuRepository.findAllByEnabledTrueOrderBySortOrderAsc())
            .thenReturn(List.of(
                new SidebarMenu(
                    "custom-report",
                    "Custom Report",
                    null,
                    "/reports",
                    "reports",
                    null,
                    1,
                    true
                )
            ));
        when(configurationRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(configurationRepository.save(any(UserMenuConfiguration.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateMenuAccess(7L, List.of("custom-report"));

        ArgumentCaptor<UserMenuConfiguration> saved =
            ArgumentCaptor.forClass(UserMenuConfiguration.class);
        verify(configurationRepository).save(saved.capture());
        assertEquals(List.of("custom-report"), response.menuKeys());
        assertEquals(true, response.custom());
        assertEquals(Set.of("custom-report"), saved.getValue().getMenuKeys());
    }

    @Test
    void resetsUserSelectionToRoleDefault() {
        User user = userWithRole(RoleName.USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(configurationRepository.findByUserId(7L))
            .thenReturn(Optional.empty());
        when(roleMenuAccessService.getMenuAccess(RoleName.USER))
            .thenReturn(new RoleMenuAccessResponse("USER", List.of("dashboard")));

        var response = service.resetMenuAccess(7L);

        verify(configurationRepository).deleteByUserId(7L);
        assertEquals(List.of("dashboard"), response.menuKeys());
        assertEquals(false, response.custom());
    }

    private User userWithRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(7L);
        user.setRole(role);
        return user;
    }
}
