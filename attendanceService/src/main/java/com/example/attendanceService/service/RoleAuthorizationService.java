package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.ForbiddenException;
import com.example.attendanceService.model.UserRole;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RoleAuthorizationService {

    public void requireAnyRole(String rolesHeader, UserRole... allowed) {
        Set<UserRole> parsed = parseRoles(rolesHeader);
        if (parsed.isEmpty()) {
            throw new ForbiddenException("Missing roles");
        }
        for (UserRole candidate : allowed) {
            if (parsed.contains(candidate)) {
                return;
            }
        }
        throw new ForbiddenException("Required role not present");
    }

    private Set<UserRole> parseRoles(String header) {
        if (!StringUtils.hasText(header)) {
            return Set.of();
        }
        return Arrays.stream(header.split(","))
            .map(String::trim)
            .map(UserRole::from)
            .filter(role -> role != null)
            .collect(Collectors.toSet());
    }
}
