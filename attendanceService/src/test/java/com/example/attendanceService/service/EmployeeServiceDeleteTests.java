package com.example.attendanceService.service;

import com.example.attendanceService.model.Employee;
import com.example.attendanceService.repository.AttendanceRecordRepository;
import com.example.attendanceService.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceDeleteTests {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private CompanyService companyService;
    @Mock private OfficeService officeService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks private EmployeeService service;

    @Test
    void deletesAttendanceRecordsBeforeEmployee() {
        Employee employee = new Employee();
        when(employeeRepository.findById(9L)).thenReturn(Optional.of(employee));

        service.deleteEmployee(9L);

        verify(attendanceRecordRepository).deleteAllByEmployeeId(9L);
        verify(employeeRepository).delete(employee);
    }
}
