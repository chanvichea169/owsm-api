package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.RoleMenuAccessRequest;
import com.owsm.AuthService.dto.UserMenuAccessResponse;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.UserMenuAccessService;
import jakarta.validation.Valid;
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

    public UserMenuAccessController(
        UserMenuAccessService userMenuAccessService,
        UserRepository userRepository
    ) {
        this.userMenuAccessService = userMenuAccessService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserMenuAccessResponse getMenuAccess(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        requireSelfOrAdmin(userId, authentication);
        return userMenuAccessService.getMenuAccess(userId);
    }

    @PutMapping
    public UserMenuAccessResponse updateMenuAccess(
        @PathVariable Long userId,
        @Valid @RequestBody RoleMenuAccessRequest request
    ) {
        return userMenuAccessService.updateMenuAccess(userId, request.menuKeys());
    }

    @DeleteMapping
    public UserMenuAccessResponse resetMenuAccess(@PathVariable Long userId) {
        return userMenuAccessService.resetMenuAccess(userId);
    }

    private void requireSelfOrAdmin(Long userId, Authentication authentication) {
        if (
            authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"))
        ) {
            return;
        }
        String principal = authentication.getName();
        Optional<User> currentUser = userRepository.findByEmail(principal)
            .or(() -> userRepository.findByUsername(principal));
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
