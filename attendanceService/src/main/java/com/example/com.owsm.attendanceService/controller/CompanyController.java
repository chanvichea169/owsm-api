package com.example.attendanceService.controller;

import com.example.attendanceService.dto.CompanyResponse;
import com.example.attendanceService.dto.CreateCompanyRequest;
import com.example.attendanceService.dto.UpdateCompanyRequest;
import com.example.attendanceService.service.CompanyLogoStorageService;
import com.example.attendanceService.service.CompanyService;
import com.example.attendanceService.service.DepartmentAccessService;
import com.example.attendanceService.service.TelegramAlertService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final DepartmentAccessService departmentAccessService;
    private final CompanyLogoStorageService logoStorageService;
    private final TelegramAlertService telegramAlertService;

    public CompanyController(
        CompanyService companyService,
        DepartmentAccessService departmentAccessService,
        CompanyLogoStorageService logoStorageService,
        TelegramAlertService telegramAlertService
    ) {
        this.companyService = companyService;
        this.departmentAccessService = departmentAccessService;
        this.logoStorageService = logoStorageService;
        this.telegramAlertService = telegramAlertService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @Valid @RequestBody CreateCompanyRequest request
    ) {
        departmentAccessService.requireAdmin(departmentAccessService.authenticate(authorization));
        var company = companyService.createCompany(request);
        return ResponseEntity.created(URI.create("/api/companies/" + company.getId()))
            .body(CompanyResponse.from(company));
    }

    @GetMapping
    public List<CompanyResponse> list(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        var context = departmentAccessService.authenticate(authorization);
        Long departmentId = departmentAccessService.requireDepartmentScope(context, null);
        return departmentId == null
            ? companyService.listCompanies()
            : List.of(companyService.getCompanyResponse(departmentId));
    }

    @GetMapping("/{companyId}")
    public CompanyResponse get(
        @PathVariable Long companyId,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            companyId
        );
        return companyService.getCompanyResponse(companyId);
    }

    @PutMapping("/{companyId}")
    public CompanyResponse update(
        @PathVariable Long companyId,
        @Valid @RequestBody UpdateCompanyRequest request,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        departmentAccessService.requireAdmin(departmentAccessService.authenticate(authorization));
        return CompanyResponse.from(companyService.updateCompany(companyId, request));
    }

    @PutMapping(value = "/{companyId}/branding", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompanyResponse updateBranding(
        @PathVariable Long companyId,
        @RequestParam String name,
        @RequestPart(required = false) MultipartFile logo,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        var context = departmentAccessService.authenticate(authorization);
        departmentAccessService.requireDepartmentBrandingAccess(context, companyId);
        return CompanyResponse.from(companyService.updateBranding(companyId, name, logo));
    }

    @GetMapping("/{companyId}/logo")
    public ResponseEntity<Resource> getLogo(@PathVariable Long companyId) {
        var company = companyService.getCompany(companyId);
        if (company.getLogoPath() == null) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Department logo not found"
            );
        }
        var file = logoStorageService.resolve(company.getLogoPath());
        String contentType;
        try {
            contentType = java.nio.file.Files.probeContentType(file);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not read department logo",
                exception
            );
        }
        return ResponseEntity.ok()
            .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
            .body(new FileSystemResource(file));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long companyId,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        departmentAccessService.requireAdmin(departmentAccessService.authenticate(authorization));
        companyService.deleteCompany(companyId);
        telegramAlertService.destructiveAction(
            "Company deleted",
            "company id: " + companyId,
            "ADMIN"
        );
        return ResponseEntity.noContent().build();
    }
}
