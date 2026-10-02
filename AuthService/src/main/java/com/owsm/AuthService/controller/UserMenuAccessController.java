package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.UserMenuAccessResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.TelegramAlertService;
import com.owsm.AuthService.service.UserMenuAccessService;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/{userId}/menu-access")
public class UserMenuAccessController {

    private final UserMenuAccessService userMenuAccessService;
    private final UserRepository userRepository;
    private final TelegramAlertService telegramAlertService;

    public UserMenuAccessController(
        UserMenuAccessService userMenuAccessService,
        UserRepository userRepository,
        TelegramAlertService telegramAlertService
    ) {
        this.userMenuAccessService = userMenuAccessService;
        this.userRepository = userRepository;
        this.telegramAlertService = telegramAlertService;
    }

    @GetMapping
    public UserMenuAccessResponse getMenuAccess(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        requireMenuAccess(userId, authentication, false);
        return userMenuAccessService.getMenuAccess(userId);
    }

    @PutMapping
    public UserMenuAccessResponse updateMenuAccess(
        @PathVariable Long userId,
        @Valid @RequestBody RoleMenuAccessRequest request,
        Authentication authentication
    ) {
        requireMenuAccess(userId, authentication, true);
        return userMenuAccessService.updateMenuAccess(userId, request.menuKeys());
    }

    @DeleteMapping
    public UserMenuAccessResponse resetMenuAccess(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        requireMenuAccess(userId, authentication, true);
        UserMenuAccessResponse response = userMenuAccessService.resetMenuAccess(userId);
        telegramAlertService.destructiveAction(
            "User menu access reset",
            "user id: " + userId,
            telegramAlertService.currentActor());
        return response;
    }

    private void requireMenuAccess(
        Long userId,
        Authentication authentication,
        boolean manage
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String principal = authentication.getName();
        Optional<User> currentUser = userRepository.findByEmail(principal)
            .or(() -> userRepository.findByUsername(principal));
        if (currentUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User manager = currentUser.get();
        if (hasRole(manager, RoleName.ADMIN)) {
            return;
        }
        if (Objects.equals(manager.getId(), userId)) {
            if (!manage) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (hasRole(manager, RoleName.HEAD_OF_DEPARTMENT)
            && manager.getDepartmentId() != null) {
            User target = userRepository.findById(userId).orElse(null);
            if (target != null
                && !hasRole(target, RoleName.ADMIN)
                && Objects.equals(manager.getDepartmentId(), target.getDepartmentId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private boolean hasRole(User user, RoleName roleName) {
        Role role = user.getRole();
        return role != null && role.getName() == roleName;
    }
}
