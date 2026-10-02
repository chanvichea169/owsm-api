package com.example.attendanceService.service;

import com.example.attendanceService.model.Company;
import com.example.attendanceService.repository.AttendanceRecordRepository;
import com.example.attendanceService.repository.CompanyRepository;
import com.example.attendanceService.repository.EmployeeRepository;
import com.example.attendanceService.repository.OfficeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceDeleteTests {

    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyLogoStorageService companyLogoStorageService;
    @Mock private AttendanceRecordRepository attendanceRecordRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private OfficeRepository officeRepository;

    @InjectMocks private CompanyService service;

    @Test
    void deletesEmployeesOfficesAndRecordsBeforeCompany() {
        Company company = new Company();
        when(companyRepository.findById(7L)).thenReturn(Optional.of(company));

        service.deleteCompany(7L);

        verify(attendanceRecordRepository).deleteAllByCompanyId(7L);
        verify(employeeRepository).deleteAllByCompanyId(7L);
        verify(officeRepository).deleteAllByCompanyId(7L);
        verify(companyRepository).delete(company);
    }
}
