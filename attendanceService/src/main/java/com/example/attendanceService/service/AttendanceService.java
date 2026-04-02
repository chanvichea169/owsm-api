package com.example.attendanceService.service;

import com.example.attendanceService.dto.AttendanceEventRequest;
import com.example.attendanceService.model.AttendanceRecord;
import com.example.attendanceService.repository.AttendanceRecordRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeService employeeService;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository, EmployeeService employeeService) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeService = employeeService;
    }

    public AttendanceRecord recordAttendance(AttendanceEventRequest request) {
        var employee = employeeService.getEmployee(request.employeeId());
        LocalDateTime recordedAt;
        if (request.recordedAt() != null) {
            recordedAt = request.recordedAt()
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        } else {
            recordedAt = LocalDateTime.now();
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setType(request.type());
        record.setRecordedAt(recordedAt);
        record.setLatitude(request.latitude());
        record.setLongitude(request.longitude());
        record.setNotes(request.notes());
        return attendanceRecordRepository.save(record);
    }

    public List<AttendanceRecord> history(Long employeeId) {
        employeeService.getEmployee(employeeId);
        return attendanceRecordRepository.findByEmployeeIdOrderByRecordedAtDesc(employeeId);
    }
}
