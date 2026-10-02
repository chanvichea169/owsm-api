package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.AdministrativeLocationResponse;
import com.owsm.AuthService.dto.CommuneRequest;
import com.owsm.AuthService.dto.DistrictRequest;
import com.owsm.AuthService.dto.ProvinceRequest;
import com.owsm.AuthService.dto.VillageRequest;
import com.owsm.AuthService.service.AdministrativeLocationService;
import com.owsm.AuthService.service.TelegramAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class AdministrativeLocationController {

    private final AdministrativeLocationService locationService;
    private final TelegramAlertService telegramAlertService;

    @PostMapping("/provinces")
    public ResponseEntity<AdministrativeLocationResponse> createProvince(@Valid @RequestBody ProvinceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createProvince(request));
    }

    @GetMapping("/provinces")
    public List<AdministrativeLocationResponse> getProvinces() {
        return locationService.getProvinces();
    }

    @GetMapping("/provinces/{code}")
    public AdministrativeLocationResponse getProvince(@PathVariable Integer code) {
        return locationService.getProvince(code);
    }

    @GetMapping("/provinces/{provinceCode}/districts")
    public List<AdministrativeLocationResponse> getDistrictsForProvince(@PathVariable Integer provinceCode) {
        return locationService.getDistricts(provinceCode);
    }

    @PutMapping("/provinces/{code}")
    public AdministrativeLocationResponse updateProvince(
            @PathVariable Integer code, @Valid @RequestBody ProvinceRequest request) {
        return locationService.updateProvince(code, request);
    }

    @DeleteMapping("/provinces/{code}")
    public ResponseEntity<Void> deleteProvince(@PathVariable Integer code) {
        locationService.deleteProvince(code);
        telegramAlertService.destructiveAction(
                "Location deleted",
                "province code: " + code,
                telegramAlertService.currentActor());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/districts")
    public ResponseEntity<AdministrativeLocationResponse> createDistrict(@Valid @RequestBody DistrictRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createDistrict(request));
    }

    @GetMapping("/districts")
    public List<AdministrativeLocationResponse> getDistricts(@RequestParam(required = false) Integer provinceCode) {
        return locationService.getDistricts(provinceCode);
    }

    @GetMapping("/districts/{code}")
    public AdministrativeLocationResponse getDistrict(@PathVariable Integer code) {
        return locationService.getDistrict(code);
    }

    @GetMapping("/districts/{districtCode}/communes")
    public List<AdministrativeLocationResponse> getCommunesForDistrict(@PathVariable Integer districtCode) {
        return locationService.getCommunes(districtCode);
    }

    @PutMapping("/districts/{code}")
    public AdministrativeLocationResponse updateDistrict(
            @PathVariable Integer code, @Valid @RequestBody DistrictRequest request) {
        return locationService.updateDistrict(code, request);
    }

    @DeleteMapping("/districts/{code}")
    public ResponseEntity<Void> deleteDistrict(@PathVariable Integer code) {
        locationService.deleteDistrict(code);
        telegramAlertService.destructiveAction(
                "Location deleted",
                "district code: " + code,
                telegramAlertService.currentActor());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/communes")
    public ResponseEntity<AdministrativeLocationResponse> createCommune(@Valid @RequestBody CommuneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createCommune(request));
    }

    @GetMapping("/communes")
    public List<AdministrativeLocationResponse> getCommunes(@RequestParam(required = false) Integer districtCode) {
        return locationService.getCommunes(districtCode);
    }

    @GetMapping("/communes/{code}")
    public AdministrativeLocationResponse getCommune(@PathVariable Integer code) {
        return locationService.getCommune(code);
    }

    @GetMapping("/communes/{communeCode}/villages")
    public List<AdministrativeLocationResponse> getVillagesForCommune(@PathVariable Integer communeCode) {
        return locationService.getVillages(communeCode);
    }

    @PutMapping("/communes/{code}")
    public AdministrativeLocationResponse updateCommune(
            @PathVariable Integer code, @Valid @RequestBody CommuneRequest request) {
        return locationService.updateCommune(code, request);
    }

    @DeleteMapping("/communes/{code}")
    public ResponseEntity<Void> deleteCommune(@PathVariable Integer code) {
        locationService.deleteCommune(code);
        telegramAlertService.destructiveAction(
                "Location deleted",
                "commune code: " + code,
                telegramAlertService.currentActor());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/villages")
    public ResponseEntity<AdministrativeLocationResponse> createVillage(@Valid @RequestBody VillageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createVillage(request));
    }

    @GetMapping("/villages")
    public List<AdministrativeLocationResponse> getVillages(@RequestParam(required = false) Integer communeCode) {
        return locationService.getVillages(communeCode);
    }

    @GetMapping("/villages/{code}")
    public AdministrativeLocationResponse getVillage(@PathVariable String code) {
        return locationService.getVillage(code);
    }

    @PutMapping("/villages/{code}")
    public AdministrativeLocationResponse updateVillage(
            @PathVariable String code, @Valid @RequestBody VillageRequest request) {
        return locationService.updateVillage(code, request);
    }

    @DeleteMapping("/villages/{code}")
    public ResponseEntity<Void> deleteVillage(@PathVariable String code) {
        locationService.deleteVillage(code);
        telegramAlertService.destructiveAction(
                "Location deleted",
                "village code: " + code,
                telegramAlertService.currentActor());
        return ResponseEntity.noContent().build();
    }
}
