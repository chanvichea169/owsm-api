package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.AuthenticationFailedException;
import com.example.attendanceService.common.exception.ResourceNotFoundException;
import com.example.attendanceService.dto.ChangePasswordRequest;
import com.example.attendanceService.dto.CreateEmployeeRequest;
import com.example.attendanceService.dto.EmployeeLoginRequest;
import com.example.attendanceService.dto.UpdateEmployeeRequest;
import com.example.attendanceService.model.Company;
import com.example.attendanceService.model.Employee;
import com.example.attendanceService.model.Office;
import com.example.attendanceService.repository.EmployeeRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyService companyService;
    private final OfficeService officeService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, CompanyService companyService, OfficeService officeService, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.companyService = companyService;
        this.officeService = officeService;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee registerEmployee(CreateEmployeeRequest request) {
        Company company = companyService.getCompany(request.companyId());
        Office office = null;
        if (request.officeId() != null) {
            office = officeService.getOffice(request.officeId());
            if (!office.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException("Office does not belong to the provided company");
            }
        }

        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setUsername(request.username());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setCompany(company);
        employee.setOffice(office);
        employee.setPassword(passwordEncoder.encode(request.password()));
        employee.setRequiresPasswordChange(true);
        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
    }

    public List<Employee> listByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Long employeeId, UpdateEmployeeRequest request) {
        Employee employee = getEmployee(employeeId);
        if (request.firstName() != null) {
            employee.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName());
        }
        if (request.username() != null) {
            employee.setUsername(request.username());
        }
        if (request.email() != null) {
            employee.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            employee.setPhoneNumber(request.phoneNumber());
        }

        Company company = employee.getCompany();
        boolean companyChanged = request.companyId() != null && !request.companyId().equals(company.getId());
        if (companyChanged) {
            company = companyService.getCompany(request.companyId());
            employee.setCompany(company);
            if (request.officeId() == null) {
                employee.setOffice(null);
            }
        }

        if (request.officeId() != null) {
            Office office = officeService.getByCompany(company.getId(), request.officeId());
            employee.setOffice(office);
        }

        if (StringUtils.hasText(request.password())) {
            employee.setPassword(passwordEncoder.encode(request.password()));
        }

        return employeeRepository.save(employee);
    }

    public void changePassword(Long employeeId, ChangePasswordRequest request) {
        Employee employee = getEmployee(employeeId);
        if (!passwordEncoder.matches(request.currentPassword(), employee.getPassword())) {
            throw new AuthenticationFailedException("Current password is incorrect");
        }
        employee.setPassword(passwordEncoder.encode(request.newPassword()));
        employee.setRequiresPasswordChange(false);
        employeeRepository.save(employee);
    }

    public void deleteEmployee(Long employeeId) {
        Employee employee = getEmployee(employeeId);
        employeeRepository.delete(employee);
    }

    public Employee authenticateEmployee(EmployeeLoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.identifier())
            .or(() -> employeeRepository.findByUsername(request.identifier()))
            .orElseThrow(() -> new AuthenticationFailedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        return employee;
    }
}
