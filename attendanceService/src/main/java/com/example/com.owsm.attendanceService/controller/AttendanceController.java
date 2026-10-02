package com.example.attendanceService.controller;

import com.example.attendanceService.dto.AttendanceEventRequest;
import com.example.attendanceService.dto.AttendanceRecordResponse;
import com.example.attendanceService.dto.AttendanceScanRequest;
import com.example.attendanceService.dto.AttendanceScanResponse;
import com.example.attendanceService.dto.EmployeeResponse;
import com.example.attendanceService.service.AttendanceService;
import com.example.attendanceService.service.DepartmentAccessService;
import com.example.attendanceService.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final DepartmentAccessService departmentAccessService;

    public AttendanceController(
        AttendanceService attendanceService,
        EmployeeService employeeService,
        DepartmentAccessService departmentAccessService
    ) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
        this.departmentAccessService = departmentAccessService;
    }

    @PostMapping("/events")
    public ResponseEntity<AttendanceRecordResponse> record(
        @Valid @RequestBody AttendanceEventRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (authorization != null && !authorization.isBlank()) {
            var employee = employeeService.getEmployee(request.employeeId());
            departmentAccessService.requireDepartmentManagement(
                departmentAccessService.authenticate(authorization),
                employee.getCompany().getId()
            );
        }
        var saved = attendanceService.recordAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceRecordResponse.from(saved));
    }

    @GetMapping("/records")
    public List<AttendanceRecordResponse> allRecords(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var context = departmentAccessService.authenticate(authorization);
        Long departmentId = departmentAccessService.requireDepartmentScope(context, null);
        var records = departmentId == null
            ? attendanceService.allHistory()
            : attendanceService.allHistory(departmentId);
        return records.stream()
            .map(AttendanceRecordResponse::from)
            .toList();
    }

    @GetMapping("/employees/{employeeId}/records")
    public List<AttendanceRecordResponse> history(
        @PathVariable Long employeeId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var employee = employeeService.getEmployee(employeeId);
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            employee.getCompany().getId()
        );
        return attendanceService.history(employeeId).stream().map(AttendanceRecordResponse::from).toList();
    }

    /**
     * Kiosk endpoint: records attendance for the employee whose QR badge was
     * scanned. No login is required — the scanned badge identifies the employee,
     * and the event type alternates check-in / check-out automatically.
     */
    @PostMapping("/scan")
    public ResponseEntity<AttendanceScanResponse> scan(@Valid @RequestBody AttendanceScanRequest request) {
        var result = attendanceService.scanAttendance(request);
        return ResponseEntity.ok(new AttendanceScanResponse(
            EmployeeResponse.from(result.employee()),
            AttendanceRecordResponse.from(result.record())
        ));
    }
}
