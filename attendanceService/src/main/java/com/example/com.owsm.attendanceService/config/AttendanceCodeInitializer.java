package com.example.attendanceService.config;

import com.example.attendanceService.repository.EmployeeRepository;
import com.example.attendanceService.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Assigns an attendance badge code to employees created before QR badges
 * existed, so every employee can be scanned at the kiosk.
 */
@Component
public class AttendanceCodeInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AttendanceCodeInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public AttendanceCodeInitializer(EmployeeRepository employeeRepository, EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        var missing = employeeRepository.findAll().stream()
            .filter(employee -> !StringUtils.hasText(employee.getAttendanceCode()))
            .toList();
        if (missing.isEmpty()) {
            return;
        }
        missing.forEach(employeeService::ensureAttendanceCode);
        log.info("Assigned attendance badge codes to {} existing employee(s)", missing.size());
    }
}
