package com.owsm.AuthService;

import com.owsm.AuthService.dto.AdministrativeLocationResponse;
import com.owsm.AuthService.dto.VillageRequest;
import com.owsm.AuthService.model.Commune;
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
