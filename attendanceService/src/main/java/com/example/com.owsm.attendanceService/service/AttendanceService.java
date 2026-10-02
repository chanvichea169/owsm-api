package com.example.attendanceService.service;

import com.example.attendanceService.dto.AttendanceEventRequest;
import com.example.attendanceService.dto.AttendanceScanRequest;
import com.example.attendanceService.model.AttendanceRecord;
import com.example.attendanceService.model.AttendanceType;
import com.example.attendanceService.model.Employee;
import com.example.attendanceService.repository.AttendanceRecordRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceService {

    /** Ignore repeated scans of the same badge/type within this window. */
    private static final long DUPLICATE_SCAN_WINDOW_SECONDS = 45L;

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

    public List<AttendanceRecord> allHistory() {
        return attendanceRecordRepository.findAllWithEmployeeOrderByRecordedAtDesc();
    }

    public List<AttendanceRecord> allHistory(Long companyId) {
        return attendanceRecordRepository.findAllWithEmployeeByCompanyOrderByRecordedAtDesc(companyId);
    }

    /**
     * Kiosk flow: resolves an employee from the scanned badge code, decides the
     * event type (explicit or alternating check-in / check-out) and records the
     * attendance event.
     */
    public ScanResult scanAttendance(AttendanceScanRequest request) {
        Employee employee = employeeService.findByAttendanceCode(request.code());
        AttendanceRecord last = attendanceRecordRepository
            .findFirstByEmployeeIdOrderByRecordedAtDesc(employee.getId())
            .orElse(null);

        AttendanceType type = request.type() != null ? request.type() : nextType(last);

        if (last != null
            && last.getType() == type
            && last.getRecordedAt() != null
            && Duration.between(last.getRecordedAt(), LocalDateTime.now()).getSeconds()
                < DUPLICATE_SCAN_WINDOW_SECONDS) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This badge was scanned a moment ago. Please wait before scanning again.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setType(type);
        record.setRecordedAt(LocalDateTime.now());
        record.setLatitude(request.latitude());
        record.setLongitude(request.longitude());
        record.setNotes(request.notes() == null || request.notes().isBlank()
            ? "Scanned via QR kiosk"
            : request.notes());
        return new ScanResult(employee, attendanceRecordRepository.save(record));
    }

    /** Alternates check-in / check-out so one scan button works all day. */
    private static AttendanceType nextType(AttendanceRecord last) {
        if (last == null
            || last.getRecordedAt() == null
            || !last.getRecordedAt().toLocalDate().equals(LocalDate.now())) {
            return AttendanceType.CHECK_IN;
        }
        return switch (last.getType()) {
            case CHECK_IN -> AttendanceType.CHECK_OUT;
            case CHECK_OUT -> AttendanceType.CHECK_IN;
            case BREAK_START -> AttendanceType.BREAK_END;
            case BREAK_END -> AttendanceType.BREAK_START;
        };
    }

    public record ScanResult(Employee employee, AttendanceRecord record) {
    }
}
