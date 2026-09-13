package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.RoleResponse;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceHandler {

    private final RoleRepository roleRepository;

    // ================= VALIDATION =================

    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.error("Username is null or empty");
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            log.error("Username contains invalid characters: {}", username);
            throw new IllegalArgumentException("Username contains invalid characters");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            log.error("Email format is invalid: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // ================= REQUEST → ENTITY =================

    public User convertToUser(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found: " + request.getRoleId()));
            user.setRole(role);
        }

        user.setPassword(request.getPassword());
        // Note: OTP, enabled, active are set explicitly in UserServiceImpl.registerUser
        // so we don't duplicate that logic here.
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    // ================= ENTITY → RESPONSE =================

    public UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());

        // Email OTP verification state
        userResponse.setEnabled(user.isEnabled());

        // Admin-controlled enable/disable — drives the UI toggle icon
        userResponse.setActive(user.isActive());

        // Nested role
        if (user.getRole() != null) {
            RoleResponse roleResponse = new RoleResponse();
            roleResponse.setId(user.getRole().getId());
            roleResponse.setName(String.valueOf(user.getRole().getName()));
            roleResponse.setDescription(user.getRole().getDescription());
            userResponse.setRole(roleResponse);
        }

        // LocalDateTime → java.util.Date
        if (user.getCreatedAt() != null) {
            userResponse.setCreatedAt(toDate(user.getCreatedAt()));
        }
        if (user.getUpdatedAt() != null) {
            userResponse.setUpdatedAt(toDate(user.getUpdatedAt()));
        }

        return userResponse;
    }

    // ================= HELPERS =================

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}