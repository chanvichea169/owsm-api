package com.example.attendanceService.service;

import com.example.attendanceService.common.exception.ResourceNotFoundException;
import com.example.attendanceService.dto.CreateOfficeRequest;
import com.example.attendanceService.dto.UpdateOfficeRequest;
import com.example.attendanceService.model.Company;
import com.example.attendanceService.model.Office;
import com.example.attendanceService.repository.OfficeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final CompanyService companyService;

    public OfficeService(OfficeRepository officeRepository, CompanyService companyService) {
        this.officeRepository = officeRepository;
        this.companyService = companyService;
    }

    public Office createOffice(Long companyId, CreateOfficeRequest request) {
        Company company = companyService.getCompany(companyId);
        Office office = new Office();
        office.setName(request.name());
        office.setCode(request.code());
        office.setAddress(request.address());
        office.setPhoneNumber(request.phoneNumber());
        office.setEmail(request.email());
        office.setCompany(company);
        return officeRepository.save(office);
    }

    public List<Office> listByCompany(Long companyId) {
        return officeRepository.findByCompanyId(companyId);
    }

    public Office getOffice(Long officeId) {
        return officeRepository.findById(officeId)
            .orElseThrow(() -> new ResourceNotFoundException("Office", officeId));
    }

    public Office getByCompany(Long companyId, Long officeId) {
    return officeRepository.findByIdAndCompanyId(officeId, companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Office", officeId));
  }

  public Office updateOffice(Long companyId, Long officeId, UpdateOfficeRequest request) {
    Office office = getByCompany(companyId, officeId);

    if (StringUtils.hasText(request.name())) {
      office.setName(request.name());
    }
    if (StringUtils.hasText(request.code())) {
      office.setCode(request.code());
    }
    if (request.address() != null) {
      office.setAddress(request.address());
    }
    if (request.phoneNumber() != null) {
      office.setPhoneNumber(request.phoneNumber());
    }
    if (request.email() != null) {
      office.setEmail(request.email());
    }

    return officeRepository.save(office);
  }

  public void deleteOffice(Long companyId, Long officeId) {
    Office office = getByCompany(companyId, officeId);
    officeRepository.delete(office);
  }
}
