package com.owsm.AuthService;

import com.owsm.AuthService.dto.LocationResponse;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.model.Commune;
import com.owsm.AuthService.model.District;
import com.owsm.AuthService.model.Province;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.Village;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.repository.VillageRepository;
import com.owsm.AuthService.service.handler.UserServiceHandler;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserLocationMappingTests {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final VillageRepository villageRepository = mock(VillageRepository.class);
    private final UserServiceHandler handler =
            new UserServiceHandler(roleRepository, villageRepository);

    @Test
    void mapsUserAddressAndFullAdministrativeLocation() {
        Province province = new Province();
        province.setProvinceCode(1);
        province.setProvinceEn("Province");
        province.setProvinceKh("ខេត្ត");

        District district = new District();
        district.setDistrictCode(2);
        district.setDistrictEn("District");
        district.setDistrictKh("ស្រុក");
        district.setProvince(province);

        Commune commune = new Commune();
        commune.setCommuneCode(3);
        commune.setCommuneEn("Commune");
        commune.setCommuneKh("ឃុំ");
        commune.setDistrict(district);

        Village village = new Village();
        village.setVillageCode("4");
        village.setVillageEn("Village");
        village.setVillageKh("ភូមិ");
        village.setCommune(commune);

        User user = new User();
        user.setStreetAddress("House 10");
        user.setVillage(village);

        UserResponse response = handler.convertToUserResponse(user);

        assertEquals("House 10", response.getStreetAddress());
        LocationResponse location = response.getLocation();
        assertNotNull(location);
        assertEquals("4", location.getVillageCode());
        assertEquals(3, location.getCommuneCode());
        assertEquals(2, location.getDistrictCode());
        assertEquals(1, location.getProvinceCode());
    }

    @Test
    void assignsVillageAndAddressFromRegistrationRequest() throws Exception {
        Village village = new Village();
        village.setVillageCode("42");
        when(villageRepository.findById("42")).thenReturn(Optional.of(village));

        UserRequest request = new UserRequest();
        request.setVillageCode("42");
        request.setStreetAddress("House 21");

        User user = handler.convertToUser(request);

        assertEquals(village, user.getVillage());
        assertEquals("House 21", user.getStreetAddress());
    }
}
