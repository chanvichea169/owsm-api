package com.example.attendanceService.controller;

import com.example.attendanceService.dto.ChangePasswordRequest;
import com.example.attendanceService.dto.CreateEmployeeRequest;
import com.example.attendanceService.dto.EmployeeLoginRequest;
import com.example.attendanceService.dto.EmployeeResponse;
import com.example.attendanceService.dto.UpdateEmployeeRequest;
import com.example.attendanceService.model.UserRole;
import com.example.attendanceService.service.EmployeeService;
import com.example.attendanceService.service.DepartmentAccessService;
import com.example.attendanceService.service.QrCodeService;
import com.example.attendanceService.service.TelegramAlertService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final DepartmentAccessService departmentAccessService;
    private final TelegramAlertService telegramAlertService;
    private final QrCodeService qrCodeService;

    public EmployeeController(
        EmployeeService employeeService,
        DepartmentAccessService departmentAccessService,
        TelegramAlertService telegramAlertService,
        QrCodeService qrCodeService
    ) {
        this.employeeService = employeeService;
        this.departmentAccessService = departmentAccessService;
        this.telegramAlertService = telegramAlertService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody CreateEmployeeRequest request
    ) {
        var context = departmentAccessService.authenticate(authorization);
        departmentAccessService.requireDepartmentManagement(context, request.companyId());
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
    public EmployeeResponse get(
        @PathVariable Long employeeId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var employee = employeeService.getEmployee(employeeId);
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            employee.getCompany().getId()
        );
        return EmployeeResponse.from(employee);
    }

    /**
     * Renders the employee's attendance QR badge as a PNG image. The QR encodes
     * the employee's stable badge code, which the kiosk scanner resolves.
     */
    @GetMapping("/{employeeId}/qr")
    public ResponseEntity<byte[]> qrCode(
        @PathVariable Long employeeId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var employee = employeeService.getEmployee(employeeId);
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            employee.getCompany().getId()
        );
        String badgeCode = employeeService.ensureAttendanceCode(employee);
        byte[] png = qrCodeService.generatePng(badgeCode, 512);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(png);
    }

    @GetMapping
    public List<EmployeeResponse> list(
        @RequestParam(required = false) Long companyId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var context = departmentAccessService.authenticate(authorization);
        Long scopedCompanyId = departmentAccessService.requireDepartmentScope(context, companyId);
        return (scopedCompanyId == null ? employeeService.listAll() : employeeService.listByCompany(scopedCompanyId))
            .stream().map(EmployeeResponse::from).toList();
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse update(
        @PathVariable Long employeeId,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestBody UpdateEmployeeRequest request
    ) {
        var context = departmentAccessService.authenticate(authorization);
        var currentEmployee = employeeService.getEmployee(employeeId);
        departmentAccessService.requireDepartmentManagement(context, currentEmployee.getCompany().getId());
        if (request.companyId() != null) {
            departmentAccessService.requireDepartmentManagement(context, request.companyId());
        }
        return EmployeeResponse.from(employeeService.updateEmployee(employeeId, request));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long employeeId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var context = departmentAccessService.authenticate(authorization);
        var employee = employeeService.getEmployee(employeeId);
        departmentAccessService.requireDepartmentManagement(context, employee.getCompany().getId());
        employeeService.deleteEmployee(employeeId);
        telegramAlertService.destructiveAction(
            "Employee deleted",
            "employee id: " + employeeId + "\ncompany id: " + employee.getCompany().getId(),
            "department management"
        );
        return ResponseEntity.noContent().build();
    }
}
