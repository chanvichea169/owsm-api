package com.owsm.AuthService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.owsm.AuthService.api.JwtUtil;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.securityaudit.service.AuthSessionService;
import com.owsm.AuthService.securityaudit.service.AuthenticationAuditService;
import com.owsm.AuthService.service.TelegramAlertService;
import com.owsm.AuthService.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class UserControllerCurrentAccessTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserController controller = new UserController(
        mock(UserService.class),
        mock(JwtUtil.class),
        userRepository,
        mock(AuthSessionService.class),
        mock(AuthenticationAuditService.class),
        mock(TelegramAlertService.class)
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesAuthenticatedPrincipalByEmail() {
        Role role = new Role();
        role.setName(RoleName.ADMIN);
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(role);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(java.util.Optional.of(admin));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ADMIN"))
            )
        );

        var response = controller.currentAccess();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("ADMIN", response.getBody().role());
        assertEquals(null, response.getBody().departmentId());
    }
}
