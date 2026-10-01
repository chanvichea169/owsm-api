package com.owsm.AuthService;

import com.owsm.AuthService.dto.AdministrativeLocationResponse;
import com.owsm.AuthService.dto.CommuneRequest;
import com.owsm.AuthService.dto.DistrictRequest;
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
import com.owsm.AuthService.service.serviceImpl.AdministrativeLocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdministrativeLocationServiceTests {

    private final ProvinceRepository provinceRepository = mock(ProvinceRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final CommuneRepository communeRepository = mock(CommuneRepository.class);
    private final VillageRepository villageRepository = mock(VillageRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AdministrativeLocationServiceImpl service = new AdministrativeLocationServiceImpl(
            provinceRepository, districtRepository, communeRepository, villageRepository, userRepository);

    @Test
    void createsVillageLinkedToItsCommuneAndPreservesCode() {
        Commune commune = new Commune();
        commune.setCommuneCode(102);
        when(communeRepository.findById(102)).thenReturn(Optional.of(commune));
        when(villageRepository.existsById("00102003")).thenReturn(false);
        when(villageRepository.save(any(Village.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VillageRequest request = new VillageRequest();
        request.setVillageCode("00102003");
        request.setVillageKh(" ភូមិថ្មី ");
        request.setVillageEn(" New Village ");
        request.setCommuneCode(102);

        AdministrativeLocationResponse response = service.createVillage(request);

        assertEquals("00102003", response.getCode());
        assertEquals("102", response.getParentCode());
        assertEquals("ភូមិថ្មី", response.getNameKh());
        assertEquals("New Village", response.getNameEn());
        verify(villageRepository).save(any(Village.class));
    }

    @Test
    void updatesCommuneCodeAndKeepsExistingVillagesAttached() {
        District district = new District();
        district.setDistrictCode(21);
        Commune commune = new Commune();
        commune.setCommuneCode(102);
        commune.setDistrict(district);
        Village village = new Village();
        village.setVillageCode("00102003");
        village.setCommune(commune);

        when(communeRepository.findById(102)).thenReturn(Optional.of(commune));
        when(communeRepository.existsById(103)).thenReturn(false);
        when(communeRepository.findById(103)).thenReturn(Optional.empty());
        when(districtRepository.findById(21)).thenReturn(Optional.of(district));
        when(villageRepository.findByCommuneCommuneCode(102)).thenReturn(List.of(village));
        when(communeRepository.save(any(Commune.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        CommuneRequest request = new CommuneRequest();
        request.setCommuneCode(103);
        request.setCommuneKh(" ឃុំថ្មី ");
        request.setCommuneEn(" New Commune ");
        request.setDistrictCode(21);

        AdministrativeLocationResponse response = service.updateCommune(102, request);

        assertEquals("103", response.getCode());
        assertEquals("21", response.getParentCode());
        assertEquals(103, village.getCommune().getCommuneCode());
        verify(villageRepository).saveAll(List.of(village));
        verify(communeRepository).delete(commune);
    }

    @Test
    void updatesDistrictCodeAndKeepsExistingCommunesAttached() {
        Province province = new Province();
        province.setProvinceCode(1);
        District district = new District();
        district.setDistrictCode(21);
        district.setProvince(province);
        Commune commune = new Commune();
        commune.setCommuneCode(102);
        commune.setDistrict(district);

        when(districtRepository.findById(21)).thenReturn(Optional.of(district));
        when(districtRepository.existsById(22)).thenReturn(false);
        when(provinceRepository.findById(1)).thenReturn(Optional.of(province));
        when(communeRepository.findByDistrictDistrictCode(21)).thenReturn(List.of(commune));
        when(districtRepository.save(any(District.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        DistrictRequest request = new DistrictRequest();
        request.setDistrictCode(22);
        request.setDistrictKh(" ស្រុកថ្មី ");
        request.setDistrictEn(" New District ");
        request.setProvinceCode(1);

        AdministrativeLocationResponse response = service.updateDistrict(21, request);

        assertEquals("22", response.getCode());
        assertEquals("1", response.getParentCode());
        assertEquals(22, commune.getDistrict().getDistrictCode());
        verify(communeRepository).saveAll(List.of(commune));
        verify(districtRepository).delete(district);
    }

    @Test
    void updatesVillageCodeAndKeepsExistingUsersAttached() {
        Commune commune = new Commune();
        commune.setCommuneCode(102);
        Village village = new Village();
        village.setVillageCode("00102003");
        village.setCommune(commune);
        User user = new User();
        user.setVillage(village);

        when(villageRepository.findById("00102003")).thenReturn(Optional.of(village));
        when(villageRepository.existsById("00102004")).thenReturn(false);
        when(communeRepository.findById(102)).thenReturn(Optional.of(commune));
        when(userRepository.findByVillageVillageCode("00102003")).thenReturn(List.of(user));
        when(villageRepository.save(any(Village.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        VillageRequest request = new VillageRequest();
        request.setVillageCode("00102004");
        request.setVillageKh(" ភូមិថ្មី ");
        request.setVillageEn(" New Village ");
        request.setCommuneCode(102);

        AdministrativeLocationResponse response = service.updateVillage("00102003", request);

        assertEquals("00102004", response.getCode());
        assertEquals("102", response.getParentCode());
        assertEquals("00102004", user.getVillage().getVillageCode());
        verify(userRepository).saveAll(List.of(user));
        verify(villageRepository).delete(village);
    }

    @Test
    void refusesToDeleteVillageAssignedToAUser() {
        Village village = new Village();
        village.setVillageCode("00102003");
        when(villageRepository.findById("00102003")).thenReturn(Optional.of(village));
        when(userRepository.countByVillageVillageCode("00102003")).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.deleteVillage("00102003"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(villageRepository, never()).delete(any(Village.class));
    }
}
