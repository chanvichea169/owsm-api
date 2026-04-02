package com.example.attendanceService.controller;

import com.example.attendanceService.dto.CompanyResponse;
import com.example.attendanceService.dto.CreateCompanyRequest;
import com.example.attendanceService.dto.UpdateCompanyRequest;
import com.example.attendanceService.model.UserRole;
import com.example.attendanceService.service.CompanyService;
import com.example.attendanceService.service.RoleAuthorizationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final RoleAuthorizationService roleAuthorizationService;

    public CompanyController(CompanyService companyService, RoleAuthorizationService roleAuthorizationService) {
        this.companyService = companyService;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(
        @RequestHeader(value = "X-User-Roles", required = false) String roles,
        @Valid @RequestBody CreateCompanyRequest request
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        var company = companyService.createCompany(request);
        return ResponseEntity.created(URI.create("/api/companies/" + company.getId()))
            .body(CompanyResponse.from(company));
    }

    @GetMapping
    public List<CompanyResponse> list() {
        return companyService.listCompanies().stream().map(CompanyResponse::from).toList();
    }

    @GetMapping("/{companyId}")
    public CompanyResponse get(@PathVariable Long companyId) {
        return CompanyResponse.from(companyService.getCompany(companyId));
    }

    @PutMapping("/{companyId}")
    public CompanyResponse update(
        @PathVariable Long companyId,
        @Valid @RequestBody UpdateCompanyRequest request,
        @RequestHeader(value = "X-User-Roles", required = false) String roles
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        return CompanyResponse.from(companyService.updateCompany(companyId, request));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long companyId,
        @RequestHeader(value = "X-User-Roles", required = false) String roles
    ) {
        roleAuthorizationService.requireAnyRole(roles, UserRole.ADMIN, UserRole.HR, UserRole.HEAD_OF_DEPARTMENT);
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
