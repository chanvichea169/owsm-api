package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.AdministrativeLocationResponse;
import com.owsm.AuthService.dto.CommuneRequest;
import com.owsm.AuthService.dto.DistrictRequest;
import com.owsm.AuthService.dto.ProvinceRequest;
import com.owsm.AuthService.dto.VillageRequest;

import java.util.List;

public interface AdministrativeLocationService {
    AdministrativeLocationResponse createProvince(ProvinceRequest request);
    AdministrativeLocationResponse getProvince(Integer code);
    List<AdministrativeLocationResponse> getProvinces();
    AdministrativeLocationResponse updateProvince(Integer code, ProvinceRequest request);
    void deleteProvince(Integer code);

    AdministrativeLocationResponse createDistrict(DistrictRequest request);
    AdministrativeLocationResponse getDistrict(Integer code);
    List<AdministrativeLocationResponse> getDistricts(Integer provinceCode);
    AdministrativeLocationResponse updateDistrict(Integer code, DistrictRequest request);
    void deleteDistrict(Integer code);

    AdministrativeLocationResponse createCommune(CommuneRequest request);
    AdministrativeLocationResponse getCommune(Integer code);
    List<AdministrativeLocationResponse> getCommunes(Integer districtCode);
    AdministrativeLocationResponse updateCommune(Integer code, CommuneRequest request);
    void deleteCommune(Integer code);

    AdministrativeLocationResponse createVillage(VillageRequest request);
    AdministrativeLocationResponse getVillage(String code);
    List<AdministrativeLocationResponse> getVillages(Integer communeCode);
    AdministrativeLocationResponse updateVillage(String code, VillageRequest request);
    void deleteVillage(String code);
}
