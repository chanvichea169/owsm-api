package com.example.attendanceService.controller;

import com.example.attendanceService.dto.CreateOfficeRequest;
import com.example.attendanceService.dto.OfficeResponse;
import com.example.attendanceService.dto.UpdateOfficeRequest;
import com.example.attendanceService.service.OfficeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/offices")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @PostMapping
    public ResponseEntity<OfficeResponse> create(
        @PathVariable Long companyId,
        @Valid @RequestBody CreateOfficeRequest request
    ) {
        var office = officeService.createOffice(companyId, request);
        return ResponseEntity.created(URI.create("/api/companies/" + companyId + "/offices/" + office.getId()))
            .body(OfficeResponse.from(office));
    }

    @GetMapping
    public List<OfficeResponse> list(@PathVariable Long companyId) {
    return officeService.listByCompany(companyId).stream().map(OfficeResponse::from).toList();
  }

  @GetMapping("/{officeId}")
  public OfficeResponse get(@PathVariable Long companyId, @PathVariable Long officeId) {
    return OfficeResponse.from(officeService.getByCompany(companyId, officeId));
  }

  @PutMapping("/{officeId}")
  public OfficeResponse update(
      @PathVariable Long companyId,
      @PathVariable Long officeId,
      @Valid @RequestBody UpdateOfficeRequest request
  ) {
    return OfficeResponse.from(officeService.updateOffice(companyId, officeId, request));
  }

  @DeleteMapping("/{officeId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long companyId,
      @PathVariable Long officeId
  ) {
    officeService.deleteOffice(companyId, officeId);
    return ResponseEntity.noContent().build();
  }
}
