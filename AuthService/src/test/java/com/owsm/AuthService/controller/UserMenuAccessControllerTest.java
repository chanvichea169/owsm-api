package com.owsm.AuthService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.UserMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.TelegramAlertService;
import com.owsm.AuthService.service.UserMenuAccessService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

class UserMenuAccessControllerTest {

    private final UserMenuAccessService menuAccessService = mock(UserMenuAccessService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserMenuAccessController controller = new UserMenuAccessController(
        menuAccessService,
        userRepository,
        mock(TelegramAlertService.class)
    );

    @Test
    void departmentHeadCanManageMenuAccessForUsersInTheirDepartment() {
        User head = user(1L, 10L, RoleName.HEAD_OF_DEPARTMENT);
        User target = user(2L, 10L, RoleName.USER);
        Authentication authentication = authentication("head@example.com");
        UserMenuAccessResponse response = new UserMenuAccessResponse(2L, List.of("home"), true);
        when(userRepository.findByEmail("head@example.com")).thenReturn(Optional.of(head));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(menuAccessService.getMenuAccess(2L)).thenReturn(response);
        when(menuAccessService.updateMenuAccess(2L, List.of("home"))).thenReturn(response);
        when(menuAccessService.resetMenuAccess(2L)).thenReturn(response);

        assertEquals(response, controller.getMenuAccess(2L, authentication));
        assertEquals(
            response,
            controller.updateMenuAccess(
                2L,
                new RoleMenuAccessRequest(List.of("home")),
                authentication
            )
        );
        assertEquals(response, controller.resetMenuAccess(2L, authentication));
    }

    @Test
    void departmentHeadCannotAccessUsersOutsideTheirDepartment() {
        User head = user(1L, 10L, RoleName.HEAD_OF_DEPARTMENT);
        User target = user(2L, 20L, RoleName.USER);
        Authentication authentication = authentication("head@example.com");
        when(userRepository.findByEmail("head@example.com")).thenReturn(Optional.of(head));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        assertForbidden(() -> controller.getMenuAccess(2L, authentication));
        assertForbidden(() -> controller.updateMenuAccess(
            2L,
            new RoleMenuAccessRequest(List.of("home")),
            authentication
        ));
        assertForbidden(() -> controller.resetMenuAccess(2L, authentication));
        verifyNoInteractions(menuAccessService);
    }

    @Test
    void departmentHeadCanReadOwnAccessButCannotChangeIt() {
        User head = user(1L, 10L, RoleName.HEAD_OF_DEPARTMENT);
        Authentication authentication = authentication("head@example.com");
        UserMenuAccessResponse response = new UserMenuAccessResponse(1L, List.of("home"), false);
        when(userRepository.findByEmail("head@example.com")).thenReturn(Optional.of(head));
        when(menuAccessService.getMenuAccess(1L)).thenReturn(response);

        assertEquals(response, controller.getMenuAccess(1L, authentication));
        assertForbidden(() -> controller.updateMenuAccess(
            1L,
            new RoleMenuAccessRequest(List.of("home")),
            authentication
        ));
        assertForbidden(() -> controller.resetMenuAccess(1L, authentication));
        verify(menuAccessService).getMenuAccess(1L);
    }

    private void assertForbidden(Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    private Authentication authentication(String principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private User user(Long id, Long departmentId, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(id);
        user.setDepartmentId(departmentId);
        user.setRole(role);
        return user;
    }
}