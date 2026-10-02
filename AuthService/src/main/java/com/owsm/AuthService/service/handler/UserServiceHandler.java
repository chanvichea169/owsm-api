package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.LocationResponse;
import com.owsm.AuthService.dto.RoleResponse;
import com.owsm.AuthService.dto.UserRequest;
import com.owsm.AuthService.dto.UserResponse;
import com.owsm.AuthService.exception.OwsmException;
import com.owsm.AuthService.model.*;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceHandler {

    private final RoleRepository roleRepository;
    private final VillageRepository villageRepository;

    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.error("Username is null or empty");
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            log.error("Username contains invalid characters: {}", username);
            throw new IllegalArgumentException("Username contains invalid characters");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        if (!email.matches("^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            log.error("Email format is invalid: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public User convertToUser(UserRequest request) throws OwsmException {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(Long.valueOf(request.getRoleId()))
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found: " + request.getRoleId()
                    ));

            user.setRole(role);
        }

        if (request.getVillageCode() != null) {
            user.setVillage(findVillageByCode(request.getVillageCode()));
        }

        user.setStreetAddress(request.getStreetAddress());
        user.setPassword(request.getPassword());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    public Village findVillageByCode(String villageCode) throws OwsmException {
        return villageRepository.findById(villageCode)
                .orElseThrow(() -> new OwsmException("VILLAGE_NOT_FOUND"));
    }

    public UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());

        userResponse.setEnabled(user.isEnabled());
        userResponse.setActive(user.isActive());
        userResponse.setStreetAddress(user.getStreetAddress());
        userResponse.setDepartmentId(user.getDepartmentId());

        if (user.getRole() != null) {
            RoleResponse roleResponse = new RoleResponse();

            roleResponse.setId(user.getRole().getId());
            roleResponse.setName(String.valueOf(user.getRole().getName()));
            roleResponse.setDescription(user.getRole().getDescription());

            userResponse.setRole(roleResponse);
        }

        if (user.getVillage() != null) {
            userResponse.setLocation(
                    mapToLocationResponse(user.getVillage())
            );
        }

        if (user.getCreatedAt() != null) {
            userResponse.setCreatedAt(
                    toDate(user.getCreatedAt())
            );
        }

        if (user.getUpdatedAt() != null) {
            userResponse.setUpdatedAt(
                    toDate(user.getUpdatedAt())
            );
        }

        return userResponse;
    }

    public LocationResponse mapToLocationResponse(Village village) {
        if (village == null) {
            return null;
        }

        LocationResponse location = new LocationResponse();

        location.setVillageCode(village.getVillageCode());
        location.setVillageEn(village.getVillageEn());
        location.setVillageKh(village.getVillageKh());

        Commune commune = village.getCommune();

        if (commune != null) {
            location.setCommuneCode(commune.getCommuneCode());
            location.setCommuneEn(commune.getCommuneEn());
            location.setCommuneKh(commune.getCommuneKh());

            District district = commune.getDistrict();

            if (district != null) {
                location.setDistrictCode(district.getDistrictCode());
                location.setDistrictEn(district.getDistrictEn());
                location.setDistrictKh(district.getDistrictKh());

                Province province = district.getProvince();

                if (province != null) {
                    location.setProvinceCode(province.getProvinceCode());
                    location.setProvinceEn(province.getProvinceEn());
                    location.setProvinceKh(province.getProvinceKh());
                }
            }
        }

        return location;
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(
                value.atZone(ZoneId.systemDefault()).toInstant()
        );
    }
}