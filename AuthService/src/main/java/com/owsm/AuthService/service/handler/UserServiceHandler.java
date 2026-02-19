package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserServiceHandler {

    @Autowired
    private RoleRepository roleRepository;

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

    public User convertToUser(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId()));
            user.setRole(role);
        }
        user.setPassword(request.getPassword());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return user;
    }

    public UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        if (user.getRole() != null) {
            userResponse.setRole(user.getRole().getName().name());
        }
        userResponse.setEnabled(user.isEnabled());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
