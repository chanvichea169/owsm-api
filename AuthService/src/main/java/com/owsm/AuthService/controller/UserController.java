package com.owsm.AuthService.controller;
import com.owsm.AuthService.dto.ChangePasswordRequest;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.dto.VerifyOtpRequest;
import com.owsm.AuthService.exception.OwsmException;
import com.owsm.AuthService.api.JwtUtil;
import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.securityaudit.service.AuthSessionService;
import com.owsm.AuthService.securityaudit.service.AuthenticationAuditService;
import com.owsm.AuthService.service.TelegramAlertService;
import com.owsm.AuthService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;
    private final AuthenticationAuditService authenticationAuditService;
    private final TelegramAlertService telegramAlertService;

    @GetMapping("/current-access")
    public ResponseEntity<CurrentAccessResponse> currentAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = findAuthenticatedUser(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        String role = user.getRole() == null || user.getRole().getName() == null
                ? null
                : user.getRole().getName().name();
        return ResponseEntity.ok(new CurrentAccessResponse(role, user.getDepartmentId()));
    }

    public record CurrentAccessResponse(String role, Long departmentId) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.registerUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            return userServiceError(e);
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> registerAdminUser(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.registerAdminUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/department")
    public ResponseEntity<?> registerDepartmentUser(@RequestBody UserRequest request) {
        User manager = requireDepartmentManager();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    userService.registerDepartmentUser(request, manager.getDepartmentId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            return userServiceError(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.loginUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (OwsmException e) {
            if ("TELEGRAM_NOT_LINKED".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            if ("OTP_DELIVERY_FAILED".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
            }
            if ("INVALID_OTP_CHANNEL".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            if (request.getEmail() == null || request.getOtp() == null ||
                    request.getEmail().isBlank() || request.getOtp().isBlank()) {
                return ResponseEntity.badRequest().body("Email and OTP are required");
            }

            UserResponse response = userService.verifyOtp(
                    request.getEmail().trim(),
                    request.getOtp().trim()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring("Bearer ".length());
        var sessionId = jwtUtil.extractSessionId(token);
        String principal = authentication.getName();
        User user = userRepository.findByEmailWithLocation(principal)
                .or(() -> userRepository.findByUsernameWithLocation(principal))
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (sessionId != null) {
            authSessionService.revoke(sessionId, user.getId());
        }
        authenticationAuditService.recordLogout(user, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            userService.resendOtp(email);
            return ResponseEntity.ok("OTP resent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User currentUser = getAuthenticatedUser();
        if (!Objects.equals(currentUser.getId(), id)
                || request.getRoleId() != null || request.getDepartmentId() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users may only update their own profile");
        }
        try {
            return ResponseEntity.ok(userService.updateUser(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (OwsmException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateAdminUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok(userService.updateAdminUser(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            HttpStatus status = switch (e.getMessage()) {
                case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                case "EMAIL_ALREADY_EXISTS", "USERNAME_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity.status(status).body(e.getMessage());
        }
    }

    @PutMapping("/department/{id}")
    public ResponseEntity<?> updateDepartmentUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {
        User manager = requireDepartmentManager();
        if (Objects.equals(manager.getId(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit your own account here");
        }
        try {
            return ResponseEntity.ok(
                    userService.updateDepartmentUser(id, request, manager.getDepartmentId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (OwsmException e) {
            return userServiceError(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireUserReadAccess(target);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        User currentUser = getAuthenticatedUser();
        if (hasRole(currentUser, RoleName.ADMIN)) {
            return ResponseEntity.ok(userService.getAllUsers());
        }
        if (hasRole(currentUser, RoleName.HEAD_OF_DEPARTMENT)
                && currentUser.getDepartmentId() != null) {
            return ResponseEntity.ok(userService.getUsersByDepartmentId(currentUser.getDepartmentId()));
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User list access is not allowed");
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateUserLocation(
            @PathVariable Long id,
            @RequestBody UserRequest request) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireSelfOrAdmin(target);
        try {
            return ResponseEntity.ok(userService.updateUserLocation(
                    id, request.getStreetAddress(), request.getVillageCode()));
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<?> getUserWithLocation(@PathVariable Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireUserReadAccess(target);
        try {
            return ResponseEntity.ok(userService.getUserWithLocation(id));
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-village/{villageCode}")
    public ResponseEntity<List<UserResponse>> getUsersByVillage(@PathVariable String villageCode) {
        requireAdminAccess();
        return ResponseEntity.ok(userService.getUsersByVillage(villageCode));
    }

    @GetMapping("/by-commune/{communeCode}")
    public ResponseEntity<List<UserResponse>> getUsersByCommune(@PathVariable Integer communeCode) {
        requireAdminAccess();
        return ResponseEntity.ok(userService.getUsersByCommune(communeCode));
    }

    @GetMapping("/by-district/{districtCode}")
    public ResponseEntity<List<UserResponse>> getUsersByDistrict(@PathVariable Integer districtCode) {
        requireAdminAccess();
        return ResponseEntity.ok(userService.getUsersByDistrict(districtCode));
    }

    @GetMapping("/by-province/{provinceCode}")
    public ResponseEntity<List<UserResponse>> getUsersByProvince(@PathVariable Integer provinceCode) {
        requireAdminAccess();
        return ResponseEntity.ok(userService.getUsersByProvince(provinceCode));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        requireAdminAccess();
        try {
            User target = userRepository.findById(id).orElse(null);
            userService.deleteUser(id);
            telegramAlertService.destructiveAction(
                    "User deleted",
                    "target: " + describeUser(target, id),
                    telegramAlertService.currentActor());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (OwsmException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/department/{id}")
    public ResponseEntity<?> deleteDepartmentUser(@PathVariable Long id) {
        User manager = requireDepartmentManager();
        if (Objects.equals(manager.getId(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete your own account");
        }
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!Objects.equals(target.getDepartmentId(), manager.getDepartmentId())
                || hasRole(target, RoleName.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is outside your department");
        }
        try {
            userService.deleteUser(id);
            telegramAlertService.destructiveAction(
                    "Department user deleted",
                    "target: " + describeUser(target, id),
                    telegramAlertService.currentActor());
            return ResponseEntity.noContent().build();
        } catch (OwsmException e) {
            return userServiceError(e);
        }
    }

    private static String describeUser(User user, Long id) {
        if (user == null) {
            return "id " + id;
        }
        String username = user.getUsername() == null ? "" : user.getUsername();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (!email.isBlank()) {
            return username.isBlank() ? email : username + " <" + email + ">";
        }
        return username.isBlank() ? ("id " + id) : username;
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        if (!Objects.equals(getAuthenticatedUser().getId(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users may only change their own password");
        }
        try {
            if (request.getCurrentPassword() == null || request.getNewPassword() == null
                    || request.getCurrentPassword().isBlank() || request.getNewPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Current and new password are required");
            }

            userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password updated successfully");
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        requireAdminAccess();
        try {
            userService.setUserEnabled(id, true);
            return ResponseEntity.ok("User enabled successfully");
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        requireAdminAccess();
        try {
            userService.setUserEnabled(id, false);
            return ResponseEntity.ok("User disabled successfully");
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        requireAdminAccess();
        try {
            boolean enabled = userService.toggleUserEnabled(id);
            return ResponseEntity.ok(Map.of(
                    "enabled", enabled,
                    "message", enabled ? "User enabled successfully" : "User disabled successfully"
            ));
        } catch (OwsmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return findAuthenticatedUser(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private java.util.Optional<User> findAuthenticatedUser(String principal) {
        return userRepository.findByEmail(principal)
                .or(() -> userRepository.findByUsername(principal));
    }

    private void requireAdminAccess() {
        if (!hasRole(getAuthenticatedUser(), RoleName.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    private User requireDepartmentManager() {
        User currentUser = getAuthenticatedUser();
        if (!hasRole(currentUser, RoleName.HEAD_OF_DEPARTMENT)
                || currentUser.getDepartmentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "A Head of Department with an assigned department is required");
        }
        return currentUser;
    }

    private ResponseEntity<?> userServiceError(OwsmException exception) {
        HttpStatus status = switch (exception.getMessage()) {
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "EMAIL_ALREADY_EXISTS", "USERNAME_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(exception.getMessage());
    }

    private void requireUserReadAccess(User target) {
        User currentUser = getAuthenticatedUser();
        if (hasRole(currentUser, RoleName.ADMIN)
                || Objects.equals(currentUser.getId(), target.getId())) {
            return;
        }
        if (hasRole(currentUser, RoleName.HEAD_OF_DEPARTMENT)
                && currentUser.getDepartmentId() != null
                && Objects.equals(currentUser.getDepartmentId(), target.getDepartmentId())
                && !hasRole(target, RoleName.ADMIN)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to this user is not allowed");
    }

    private void requireSelfOrAdmin(User target) {
        User currentUser = getAuthenticatedUser();
        if (!hasRole(currentUser, RoleName.ADMIN)
                && !Objects.equals(currentUser.getId(), target.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users may only update their own profile");
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRole() != null && user.getRole().getName() == roleName;
    }
}