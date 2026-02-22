package com.owsm.AuthService.service.handler;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserProfileServiceHandler {
    public UserProfileResponse convertToResponse(UserProfile entity) {
        if (entity == null) return null;

        UserProfileResponse response = new UserProfileResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setAvatarUrl(entity.getAvatarUrl());
        response.setBio(entity.getBio());
        response.setAddress(entity.getAddress());
        response.setBirthDate(entity.getBirthDate());
        if (entity.getUser() != null) {
            response.setUser(new UserProfileResponse.UserInnerResponse(entity.getUser().getId()));
        }

        return response;
    }
    public UserProfile convertToEntity(UserProfileRequest response) {
        UserProfile entity = new UserProfile();
        entity.setFirstName(response.getFirstName());
        entity.setLastName(response.getLastName());
        entity.setPhoneNumber(response.getPhoneNumber());
        entity.setAddress(response.getAddress());
        entity.setAvatarUrl(response.getAvatarUrl());
        entity.setBio(response.getBio());
        entity.setBirthDate(response.getBirthDate());
        return entity;
    }
}
