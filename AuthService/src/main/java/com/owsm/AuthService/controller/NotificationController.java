package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.NotificationResponse;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.NotificationType;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public List<NotificationResponse> listMine() {
        return notificationService.listForUser(getAuthenticatedUser().getId());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id, getAuthenticatedUser().getId());
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(getAuthenticatedUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        User currentUser = getAuthenticatedUser();
        if (currentUser.getRole() == null || currentUser.getRole().getName() != RoleName.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        NotificationResponse response = notificationService.create(
                request.userId(), request.title(), request.message(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(authentication.getName())
                .or(() -> userRepository.findByUsername(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public record CreateNotificationRequest(
            @NotNull Long userId,
            @NotBlank @Size(max = 160) String title,
            @NotBlank String message,
            NotificationType type
    ) {
    }
}