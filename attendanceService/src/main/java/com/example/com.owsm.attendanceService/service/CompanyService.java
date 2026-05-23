package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.ResourceNotFoundException;
import com.example.attendanceService.dto.CreateCompanyRequest;
import com.example.attendanceService.dto.UpdateCompanyRequest;
import com.example.attendanceService.model.Company;
import com.example.attendanceService.repository.CompanyRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(CreateCompanyRequest request) {
        Company company = new Company();
        company.setName(request.name());
        company.setCode(request.code());
        company.setAddress(request.address());
        company.setPhoneNumber(request.phoneNumber());
        company.setEmail(request.email());
        return companyRepository.save(company);
    }

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

    public void deleteCompany(Long companyId) {
        Company company = getCompany(companyId);
        companyRepository.delete(company);
    }

    public List<Company> listCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }
}
