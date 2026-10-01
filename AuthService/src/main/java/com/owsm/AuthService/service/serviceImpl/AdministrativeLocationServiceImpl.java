package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.dto.AdministrativeLocationResponse;
import com.owsm.AuthService.dto.CommuneRequest;
import com.owsm.AuthService.dto.DistrictRequest;
import com.owsm.AuthService.dto.ProvinceRequest;
import com.owsm.AuthService.dto.VillageRequest;
import com.owsm.AuthService.model.Commune;
import com.owsm.AuthService.model.District;
import com.owsm.AuthService.model.Province;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.Village;
import com.owsm.AuthService.repository.CommuneRepository;
import com.owsm.AuthService.repository.DistrictRepository;
import com.owsm.AuthService.repository.ProvinceRepository;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.repository.VillageRepository;
import com.owsm.AuthService.service.AdministrativeLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdministrativeLocationServiceImpl implements AdministrativeLocationService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final VillageRepository villageRepository;
    private final UserRepository userRepository;

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse createProvince(ProvinceRequest request) {
        ensureNewCode(provinceRepository.existsById(request.getProvinceCode()), "Province", request.getProvinceCode());
        Province province = new Province();
        province.setProvinceCode(request.getProvinceCode());
        province.setProvinceKh(request.getProvinceKh().trim());
        province.setProvinceEn(request.getProvinceEn().trim());
        province.setStatus(request.getStatus());
        return toResponse(provinceRepository.save(province));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'province:' + #code")
    public AdministrativeLocationResponse getProvince(Integer code) {
        return toResponse(requireProvince(code));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'provinces'")
    public List<AdministrativeLocationResponse> getProvinces() {
        return provinceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse updateProvince(Integer code, ProvinceRequest request) {
        requireMatchingCode(code, request.getProvinceCode());
        Province province = requireProvince(code);
        province.setProvinceKh(request.getProvinceKh().trim());
        province.setProvinceEn(request.getProvinceEn().trim());
        province.setStatus(request.getStatus());
        return toResponse(provinceRepository.save(province));
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public void deleteProvince(Integer code) {
        Province province = requireProvince(code);
        if (districtRepository.countByProvinceProvinceCode(code) > 0) {
            throw conflict("Province has districts and cannot be deleted");
        }
        provinceRepository.delete(province);
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse createDistrict(DistrictRequest request) {
        ensureNewCode(districtRepository.existsById(request.getDistrictCode()), "District", request.getDistrictCode());
        District district = new District();
        district.setDistrictCode(request.getDistrictCode());
        district.setDistrictKh(request.getDistrictKh().trim());
        district.setDistrictEn(request.getDistrictEn().trim());
        district.setProvince(requireProvince(request.getProvinceCode()));
        return toResponse(districtRepository.save(district));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'district:' + #code")
    public AdministrativeLocationResponse getDistrict(Integer code) {
        return toResponse(requireDistrict(code));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'districts:' + #provinceCode")
    public List<AdministrativeLocationResponse> getDistricts(Integer provinceCode) {
        if (provinceCode == null) {
            return districtRepository.findAll().stream().map(this::toResponse).toList();
        }
        requireProvince(provinceCode);
        return districtRepository.findByProvinceProvinceCode(provinceCode).stream().map(this::toResponse).toList();
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse updateDistrict(Integer code, DistrictRequest request) {
        District district = requireDistrict(code);
        Province province = requireProvince(request.getProvinceCode());
        if (!code.equals(request.getDistrictCode())) {
            ensureNewCode(
                    districtRepository.existsById(request.getDistrictCode()),
                    "District",
                    request.getDistrictCode());
            District updatedDistrict = new District();
            updatedDistrict.setDistrictCode(request.getDistrictCode());
            updatedDistrict.setDistrictKh(request.getDistrictKh().trim());
            updatedDistrict.setDistrictEn(request.getDistrictEn().trim());
            updatedDistrict.setProvince(province);
            districtRepository.save(updatedDistrict);
            districtRepository.flush();

            List<Commune> communes = communeRepository.findByDistrictDistrictCode(code);
            for (Commune commune : communes) {
                commune.setDistrict(updatedDistrict);
            }
            communeRepository.saveAll(communes);
            communeRepository.flush();
            districtRepository.delete(district);
            districtRepository.flush();
            return toResponse(updatedDistrict);
        }
        district.setDistrictKh(request.getDistrictKh().trim());
        district.setDistrictEn(request.getDistrictEn().trim());
        district.setProvince(province);
        return toResponse(districtRepository.save(district));
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public void deleteDistrict(Integer code) {
        District district = requireDistrict(code);
        if (communeRepository.countByDistrictDistrictCode(code) > 0) {
            throw conflict("District has communes and cannot be deleted");
        }
        districtRepository.delete(district);
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse createCommune(CommuneRequest request) {
        ensureNewCode(communeRepository.existsById(request.getCommuneCode()), "Commune", request.getCommuneCode());
        Commune commune = new Commune();
        commune.setCommuneCode(request.getCommuneCode());
        commune.setCommuneKh(request.getCommuneKh().trim());
        commune.setCommuneEn(request.getCommuneEn().trim());
        commune.setDistrict(requireDistrict(request.getDistrictCode()));
        return toResponse(communeRepository.save(commune));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'commune:' + #code")
    public AdministrativeLocationResponse getCommune(Integer code) {
        return toResponse(requireCommune(code));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'communes:' + #districtCode")
    public List<AdministrativeLocationResponse> getCommunes(Integer districtCode) {
        if (districtCode == null) {
            return communeRepository.findAll().stream().map(this::toResponse).toList();
        }
        requireDistrict(districtCode);
        return communeRepository.findByDistrictDistrictCode(districtCode).stream().map(this::toResponse).toList();
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse updateCommune(Integer code, CommuneRequest request) {
        Commune commune = requireCommune(code);
        District district = requireDistrict(request.getDistrictCode());
        if (!code.equals(request.getCommuneCode())) {
            ensureNewCode(
                    communeRepository.existsById(request.getCommuneCode()),
                    "Commune",
                    request.getCommuneCode());
            Commune updatedCommune = new Commune();
            updatedCommune.setCommuneCode(request.getCommuneCode());
            updatedCommune.setCommuneKh(request.getCommuneKh().trim());
            updatedCommune.setCommuneEn(request.getCommuneEn().trim());
            updatedCommune.setDistrict(district);
            communeRepository.save(updatedCommune);
            communeRepository.flush();

            List<Village> villages = villageRepository.findByCommuneCommuneCode(code);
            for (Village village : villages) {
                village.setCommune(updatedCommune);
            }
            villageRepository.saveAll(villages);
            villageRepository.flush();
            communeRepository.delete(commune);
            communeRepository.flush();
            return toResponse(updatedCommune);
        }
        commune.setDistrict(district);
        commune.setCommuneKh(request.getCommuneKh().trim());
        commune.setCommuneEn(request.getCommuneEn().trim());
        return toResponse(communeRepository.save(commune));
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public void deleteCommune(Integer code) {
        Commune commune = requireCommune(code);
        if (villageRepository.countByCommuneCommuneCode(code) > 0) {
            throw conflict("Commune has villages and cannot be deleted");
        }
        communeRepository.delete(commune);
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse createVillage(VillageRequest request) {
        ensureNewCode(villageRepository.existsById(key(request.getVillageCode())), "Village", request.getVillageCode());
        Village village = new Village();
        village.setVillageCode(key(request.getVillageCode()));
        village.setVillageKh(request.getVillageKh().trim());
        village.setVillageEn(request.getVillageEn().trim());
        village.setCommune(requireCommune(request.getCommuneCode()));
        return toResponse(villageRepository.save(village));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'village:' + #code")
    public AdministrativeLocationResponse getVillage(String code) {
        return toResponse(requireVillage(code));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "administrative-locations", key = "'villages:' + #communeCode")
    public List<AdministrativeLocationResponse> getVillages(Integer communeCode) {
        if (communeCode == null) {
            return villageRepository.findAll().stream().map(this::toResponse).toList();
        }
        requireCommune(communeCode);
        return villageRepository.findByCommuneCommuneCode(communeCode).stream().map(this::toResponse).toList();
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public AdministrativeLocationResponse updateVillage(String code, VillageRequest request) {
        Village village = requireVillage(code);
        Commune commune = requireCommune(request.getCommuneCode());
        if (!key(code).equals(key(request.getVillageCode()))) {
            ensureNewCode(
                    villageRepository.existsById(key(request.getVillageCode())),
                    "Village",
                    request.getVillageCode());
            Village updatedVillage = new Village();
            updatedVillage.setVillageCode(key(request.getVillageCode()));
            updatedVillage.setVillageKh(request.getVillageKh().trim());
            updatedVillage.setVillageEn(request.getVillageEn().trim());
            updatedVillage.setCommune(commune);
            villageRepository.save(updatedVillage);
            villageRepository.flush();

            List<User> users = userRepository.findByVillageVillageCode(key(code));
            for (User user : users) {
                user.setVillage(updatedVillage);
            }
            userRepository.saveAll(users);
            userRepository.flush();
            villageRepository.delete(village);
            villageRepository.flush();
            return toResponse(updatedVillage);
        }
        village.setCommune(commune);
        village.setVillageKh(request.getVillageKh().trim());
        village.setVillageEn(request.getVillageEn().trim());
        return toResponse(villageRepository.save(village));
    }

    @Override
    @CacheEvict(cacheNames = "administrative-locations", allEntries = true)
    public void deleteVillage(String code) {
        Village village = requireVillage(code);
        if (userRepository.countByVillageVillageCode(key(code)) > 0) {
            throw conflict("Village is assigned to users and cannot be deleted");
        }
        villageRepository.delete(village);
    }

    private Province requireProvince(Integer code) {
        return provinceRepository.findById(code)
                .orElseThrow(() -> notFound("Province", code));
    }

    private District requireDistrict(Integer code) {
        return districtRepository.findById(code)
                .orElseThrow(() -> notFound("District", code));
    }

    private Commune requireCommune(Integer code) {
        return communeRepository.findById(code)
                .orElseThrow(() -> notFound("Commune", code));
    }

    private Village requireVillage(String code) {
        return villageRepository.findById(key(code))
                .orElseThrow(() -> notFound("Village", code));
    }

    private void ensureNewCode(boolean exists, String type, Object code) {
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, type + " code already exists: " + code);
        }
    }

    private void requireMatchingCode(Object pathCode, Object requestCode) {
        if (!pathCode.equals(requestCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The code in the request must match the path code");
        }
    }

    private String key(String code) {
        return code.trim();
    }

    private ResponseStatusException notFound(String type, Object code) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found: " + code);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private AdministrativeLocationResponse toResponse(Province province) {
        AdministrativeLocationResponse response = response(
                province.getProvinceCode(), province.getProvinceKh(), province.getProvinceEn(), null);
        response.setStatus(province.getStatus());
        return response;
    }

    private AdministrativeLocationResponse toResponse(District district) {
        return response(district.getDistrictCode(), district.getDistrictKh(), district.getDistrictEn(),
                district.getProvince().getProvinceCode());
    }

    private AdministrativeLocationResponse toResponse(Commune commune) {
        return response(commune.getCommuneCode(), commune.getCommuneKh(), commune.getCommuneEn(),
                commune.getDistrict().getDistrictCode());
    }

    private AdministrativeLocationResponse toResponse(Village village) {
        return response(village.getVillageCode(), village.getVillageKh(), village.getVillageEn(),
                village.getCommune().getCommuneCode());
    }

    private AdministrativeLocationResponse response(Object code, String nameKh, String nameEn, Object parentCode) {
        AdministrativeLocationResponse response = new AdministrativeLocationResponse();
        response.setCode(String.valueOf(code));
        response.setNameKh(nameKh);
        response.setNameEn(nameEn);
        response.setParentCode(parentCode == null ? null : String.valueOf(parentCode));
        return response;
    }
}
