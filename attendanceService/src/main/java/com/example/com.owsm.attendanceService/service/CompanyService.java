package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.ResourceNotFoundException;
import com.example.attendanceService.dto.CreateCompanyRequest;
import com.example.attendanceService.dto.CompanyResponse;
import com.example.attendanceService.dto.UpdateCompanyRequest;
import com.example.attendanceService.model.Company;
import com.example.attendanceService.repository.AttendanceRecordRepository;
import com.example.attendanceService.repository.CompanyRepository;
import com.example.attendanceService.repository.EmployeeRepository;
import com.example.attendanceService.repository.OfficeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyLogoStorageService logoStorageService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeRepository officeRepository;

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyLogoStorageService logoStorageService,
        AttendanceRecordRepository attendanceRecordRepository,
        EmployeeRepository employeeRepository,
        OfficeRepository officeRepository
    ) {
        this.companyRepository = companyRepository;
        this.logoStorageService = logoStorageService;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.officeRepository = officeRepository;
    }

    @CacheEvict(cacheNames = {"attendance-companies", "attendance-company"}, allEntries = true)
    public Company createCompany(CreateCompanyRequest request) {
        Company company = new Company();
        company.setName(request.name());
        company.setCode(request.code());
        company.setAddress(request.address());
        company.setPhoneNumber(request.phoneNumber());
        company.setEmail(request.email());
        return companyRepository.save(company);
    }

    @CacheEvict(cacheNames = {"attendance-companies", "attendance-company"}, allEntries = true)
    public Company updateCompany(Long companyId, UpdateCompanyRequest request) {
        Company company = getCompany(companyId);
        if (request.name() != null) {
            company.setName(request.name());
        }
        if (request.code() != null) {
            company.setCode(request.code());
        }
        if (request.address() != null) {
            company.setAddress(request.address());
        }
        if (request.phoneNumber() != null) {
            company.setPhoneNumber(request.phoneNumber());
        }
        if (request.email() != null) {
            company.setEmail(request.email());
        }
        return companyRepository.save(company);
    }

    @CacheEvict(cacheNames = {"attendance-companies", "attendance-company"}, allEntries = true)
    public Company updateBranding(Long companyId, String name, MultipartFile logo) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Department name is required");
        }
        Company company = getCompany(companyId);
        company.setName(name.trim());
        if (logo != null && !logo.isEmpty()) {
            company.setLogoPath(logoStorageService.store(logo));
        }
        return companyRepository.save(company);
    }

    @Transactional
    @CacheEvict(cacheNames = {"attendance-companies", "attendance-company"}, allEntries = true)
    public void deleteCompany(Long companyId) {
        Company company = getCompany(companyId);
        /* The foreign keys behind employees, offices and attendance records have
           no cascade rule, so clear the rows that belong to this department
           before deleting it. */
        attendanceRecordRepository.deleteAllByCompanyId(companyId);
        employeeRepository.deleteAllByCompanyId(companyId);
        officeRepository.deleteAllByCompanyId(companyId);
        companyRepository.delete(company);
    }

    @Cacheable(cacheNames = "attendance-companies", key = "'bilingual-v1-all'")
    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAll().stream().map(CompanyResponse::from).toList();
    }

    @Cacheable(cacheNames = "attendance-company", key = "'bilingual-v1-' + #id")
    public CompanyResponse getCompanyResponse(Long id) {
        return CompanyResponse.from(getCompany(id));
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }
}
