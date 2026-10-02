package com.example.attendanceService.service;

import com.example.attendanceService.model.Office;
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
class OfficeServiceDeleteTests {

    @Mock private OfficeRepository officeRepository;
    @Mock private CompanyService companyService;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks private OfficeService service;

    @Test
    void detachesEmployeesBeforeDeletingOffice() {
        Office office = new Office();
        when(officeRepository.findByIdAndCompanyId(5L, 3L)).thenReturn(Optional.of(office));

        service.deleteOffice(3L, 5L);

        verify(employeeRepository).detachOffice(5L);
        verify(officeRepository).delete(office);
    }
}
