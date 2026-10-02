package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.MenuActionGrant;
import com.owsm.AuthService.dto.RoleActionGrantRequest;
import com.owsm.AuthService.dto.RoleActionGrantResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.RoleActionPermission;
import com.owsm.AuthService.model.RoleMenuConfiguration;
import com.owsm.AuthService.repository.RoleActionPermissionRepository;
import com.owsm.AuthService.repository.RoleMenuConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

    @Mock
    private RoleActionPermissionRepository permissionRepository;

    @Mock
    private RoleMenuConfigurationRepository menuRepository;

    @InjectMocks
    private RolePermissionService service;

    @Test
    void savesOnlyActionsForPagesTheRoleCanView() {
        RoleMenuConfiguration config =
            new RoleMenuConfiguration(RoleName.OFFICER, Set.of("employee"));
        when(menuRepository.findByRoleName(RoleName.OFFICER))
            .thenReturn(Optional.of(config));

        RoleActionGrantResponse response = service.updateRoleGrants(
            RoleName.OFFICER,
            new RoleActionGrantRequest(List.of(
                new MenuActionGrant("employee", List.of("create", "delete")),
                new MenuActionGrant("settings", List.of("CREATE"))
            ))
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleActionPermission>> captor =
            ArgumentCaptor.forClass(List.class);
        verify(permissionRepository).deleteAllByRoleName(RoleName.OFFICER);
        verify(permissionRepository).saveAll(captor.capture());

        List<RoleActionPermission> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertEquals(
            Set.of("CREATE", "DELETE"),
            saved.stream().map(RoleActionPermission::getAction).collect(Collectors.toSet())
        );
        assertEquals(
            List.of("employee"),
            saved.stream().map(RoleActionPermission::getMenuKey).distinct().toList()
        );
        assertEquals(1, response.grants().size());
        assertEquals("employee", response.grants().get(0).menuKey());
    }

    @Test
    void returnsGroupedGrantsForRole() {
        when(permissionRepository.findByRoleName(RoleName.ADMIN)).thenReturn(List.of(
            new RoleActionPermission(RoleName.ADMIN, "employee", "EDIT"),
            new RoleActionPermission(RoleName.ADMIN, "employee", "CREATE"),
            new RoleActionPermission(RoleName.ADMIN, "settings", "DELETE")
        ));

        RoleActionGrantResponse response = service.getRoleGrants(RoleName.ADMIN);

        assertEquals("ADMIN", response.roleName());
        assertEquals(2, response.grants().size());
        assertEquals(
            List.of("employee", "settings"),
            response.grants().stream().map(MenuActionGrant::menuKey).toList()
        );
        assertEquals(List.of("CREATE", "EDIT"), response.grants().get(0).actions());
    }
}
