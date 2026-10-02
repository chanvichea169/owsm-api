package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.ForbiddenException;
import com.example.attendanceService.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepartmentAccessService {

    private final RestClient authClient;

    public DepartmentAccessService(
        @Value("${auth.service.uri:http://localhost:8081}") String authServiceUri
    ) {
        this.authClient = RestClient.builder().baseUrl(authServiceUri).build();
    }

    public AccessContext authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token required");
        }
        try {
            CurrentAccess response = authClient.get()
                .uri("/api/users/current-access")
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(CurrentAccess.class);
            if (response == null || response.role() == null) {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User access information is unavailable"
                );
            }
            return new AccessContext(UserRole.from(response.role()), response.departmentId());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new ResponseStatusException(
                    exception.getStatusCode(),
                    "Bearer token is invalid or revoked",
                    exception
                );
            }
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Could not validate the bearer token",
                exception
            );
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Could not reach the authentication service",
                exception
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token", exception);
        }
    }

    public void requireAdmin(AccessContext context) {
        if (context.role() != UserRole.ADMIN) {
            throw new ForbiddenException("Admin role required");
        }
    }

    public Long requireDepartmentScope(AccessContext context, Long requestedCompanyId) {
        if (context.role() == UserRole.ADMIN) {
            return requestedCompanyId;
        }
        if (context.role() != UserRole.HR
            && context.role() != UserRole.HEAD_OF_DEPARTMENT
            && context.role() != UserRole.OFFICER
            && context.role() != UserRole.USER) {
            throw new ForbiddenException("A department role is required");
        }
        if (context.departmentId() == null) {
            throw new ForbiddenException("User is not assigned to a department");
        }
        if (requestedCompanyId != null && !requestedCompanyId.equals(context.departmentId())) {
            throw new ForbiddenException("Access to another department is forbidden");
        }
        return context.departmentId();
    }

    public Long requireDepartmentManagement(AccessContext context, Long requestedCompanyId) {
        if (context.role() != UserRole.ADMIN
            && context.role() != UserRole.HR
            && context.role() != UserRole.HEAD_OF_DEPARTMENT) {
            throw new ForbiddenException("HR or Head of Department role required");
        }
        return requireDepartmentScope(context, requestedCompanyId);
    }

    public void requireDepartmentBrandingAccess(AccessContext context, Long companyId) {
        if (context.role() == UserRole.ADMIN) {
            return;
        }
        if (context.role() != UserRole.HEAD_OF_DEPARTMENT) {
            throw new ForbiddenException("Only Admin or Head of Department can update department branding");
        }
        requireDepartmentScope(context, companyId);
    }

    public record AccessContext(UserRole role, Long departmentId) {
    }

    private record CurrentAccess(String role, Long departmentId) {
    }
}
