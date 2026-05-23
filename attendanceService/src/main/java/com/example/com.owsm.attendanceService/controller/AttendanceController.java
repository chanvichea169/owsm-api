package com.example.attendanceService.controller;

import com.example.attendanceService.dto.AttendanceEventRequest;
import com.example.attendanceService.dto.AttendanceRecordResponse;
import com.example.attendanceService.service.AttendanceService;
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

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/events")
    public ResponseEntity<AttendanceRecordResponse> record(@Valid @RequestBody AttendanceEventRequest request) {
        var saved = attendanceService.recordAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceRecordResponse.from(saved));
    }

    @GetMapping("/employees/{employeeId}/records")
    public List<AttendanceRecordResponse> history(@PathVariable Long employeeId) {
        return attendanceService.history(employeeId).stream().map(AttendanceRecordResponse::from).toList();
    }
}
