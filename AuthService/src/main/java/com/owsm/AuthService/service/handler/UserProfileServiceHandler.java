package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserProfileServiceHandler {

    private static final String BASE_IMAGE_URL = "http://localhost:9001/uploads/profiles/";

    public UserProfileResponse convertToResponse(UserProfile entity) {
        if (entity == null) return null;

        UserProfileResponse response = new UserProfileResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setPhoneNumber(entity.getPhoneNumber());

        if (entity.getAvatarUrl() != null && !entity.getAvatarUrl().isBlank()) {
            response.setAvatarUrl(BASE_IMAGE_URL + entity.getAvatarUrl());
        }

        response.setBio(entity.getBio());
        response.setAddress(entity.getAddress());
        response.setBirthDate(entity.getBirthDate());

        if (entity.getUser() != null) {
            response.setUser(
                    new UserProfileResponse.UserInnerResponse(entity.getUser().getId())
            );
        }

        return response;
    }
    public UserProfile convertToEntity(UserProfileRequest request) {
        UserProfile entity = new UserProfile();
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setAddress(request.getAddress());
        entity.setBio(request.getBio());
        if (request.getBirthDate() != null) {
            entity.setBirthDate(
                    Date.from(request.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
            );
        }

        return entity;
    }

    public List<UserProfileResponse> convertToResponseList(List<UserProfile> entities) {
        return entities.stream()
                .map(this::convertToResponse)
                .toList();
    }
}