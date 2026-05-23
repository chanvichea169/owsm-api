package com.example.attendanceService.controller;

import com.example.attendanceService.dto.ChangePasswordRequest;
import com.example.attendanceService.dto.CreateEmployeeRequest;
import com.example.attendanceService.dto.EmployeeLoginRequest;
import com.example.attendanceService.dto.EmployeeResponse;
import com.example.attendanceService.dto.UpdateEmployeeRequest;
import com.example.attendanceService.model.UserRole;
import com.example.attendanceService.service.EmployeeService;
import com.example.attendanceService.service.RoleAuthorizationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleAuthorizationService roleAuthorizationService;

    public EmployeeController(EmployeeService employeeService, RoleAuthorizationService roleAuthorizationService) {
        this.employeeService = employeeService;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
        @RequestHeader(value = "X-User-Roles", required = false) String roles,
        @Valid @RequestBody CreateEmployeeRequest request
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        var employee = employeeService.registerEmployee(request);
        return ResponseEntity.created(URI.create("/api/employees/" + employee.getId()))
            .body(EmployeeResponse.from(employee));
    }

    @PostMapping("/login")
    public EmployeeResponse login(@Valid @RequestBody EmployeeLoginRequest request) {
        var employee = employeeService.authenticateEmployee(request);
        return EmployeeResponse.from(employee);
    }

    @PostMapping("/{employeeId}/change-password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long employeeId,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        employeeService.changePassword(employeeId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponse get(@PathVariable Long employeeId) {
        return EmployeeResponse.from(employeeService.getEmployee(employeeId));
    }

    @GetMapping
    public List<EmployeeResponse> list(@RequestParam(required = false) Long companyId) {
        return (companyId == null ? employeeService.listAll() : employeeService.listByCompany(companyId))
            .stream().map(EmployeeResponse::from).toList();
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse update(
        @PathVariable Long employeeId,
        @RequestHeader(value = "X-User-Roles", required = false) String roles,
        @RequestBody UpdateEmployeeRequest request
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        return EmployeeResponse.from(employeeService.updateEmployee(employeeId, request));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long employeeId,
        @RequestHeader(value = "X-User-Roles", required = false) String roles
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }
}
