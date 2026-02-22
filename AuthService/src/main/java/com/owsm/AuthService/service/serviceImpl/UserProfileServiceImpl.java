package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.model.UserProfile;
import com.owsm.AuthService.repository.UserProfileRepository;
import com.owsm.AuthService.service.UserProfileService;
import com.owsm.AuthService.service.handler.UserProfileServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private UserProfileServiceHandler handler;

    @Override
    public UserProfileResponse getProfileById(Long id) {
        UserProfile entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return handler.convertToResponse(entity);
    }
    @Override
    public UserProfileResponse createProfile(UserProfileRequest profile) {
        UserProfile entity = handler.convertToEntity(profile);
        UserProfile savedProfile = repository.save(entity);
        return handler.convertToResponse(savedProfile);
    }
}