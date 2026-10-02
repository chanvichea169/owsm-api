package com.example.attendanceService.controller;

import com.example.attendanceService.dto.CreateOfficeRequest;
import com.example.attendanceService.dto.OfficeResponse;
import com.example.attendanceService.dto.UpdateOfficeRequest;
import com.example.attendanceService.service.DepartmentAccessService;
import com.example.attendanceService.service.OfficeService;
import com.example.attendanceService.service.TelegramAlertService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/offices")
public class OfficeController {

    private final OfficeService officeService;
    private final DepartmentAccessService departmentAccessService;
    private final TelegramAlertService telegramAlertService;

    public OfficeController(
        OfficeService officeService,
        DepartmentAccessService departmentAccessService,
        TelegramAlertService telegramAlertService
    ) {
        this.officeService = officeService;
        this.departmentAccessService = departmentAccessService;
        this.telegramAlertService = telegramAlertService;
    }

    @PostMapping
    public ResponseEntity<OfficeResponse> create(
        @PathVariable Long companyId,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody CreateOfficeRequest request
    ) {
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            companyId
        );
        var office = officeService.createOffice(companyId, request);
        return ResponseEntity.created(URI.create("/api/companies/" + companyId + "/offices/" + office.getId()))
            .body(OfficeResponse.from(office));
    }

    @GetMapping
    public List<OfficeResponse> list(
        @PathVariable Long companyId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        departmentAccessService.requireDepartmentScope(
            departmentAccessService.authenticate(authorization),
            companyId
        );
        return officeService.listByCompany(companyId).stream().map(OfficeResponse::from).toList();
  }

  @GetMapping("/{officeId}")
  public OfficeResponse get(
      @PathVariable Long companyId,
      @PathVariable Long officeId,
      @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    departmentAccessService.requireDepartmentManagement(
        departmentAccessService.authenticate(authorization),
        companyId
    );
    return OfficeResponse.from(officeService.getByCompany(companyId, officeId));
  }

  @PutMapping("/{officeId}")
  public OfficeResponse update(
      @PathVariable Long companyId,
      @PathVariable Long officeId,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @Valid @RequestBody UpdateOfficeRequest request
  ) {
    departmentAccessService.requireDepartmentManagement(
        departmentAccessService.authenticate(authorization),
        companyId
    );
    return OfficeResponse.from(officeService.updateOffice(companyId, officeId, request));
  }

  @DeleteMapping("/{officeId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long companyId,
      @PathVariable Long officeId,
      @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    departmentAccessService.requireDepartmentManagement(
        departmentAccessService.authenticate(authorization),
        companyId
    );
    officeService.deleteOffice(companyId, officeId);
    telegramAlertService.destructiveAction(
      "Office deleted",
      "company id: " + companyId + "\noffice id: " + officeId,
      "department management"
    );
    return ResponseEntity.noContent().build();
  }
}
