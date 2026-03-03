package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserProfileService {
    UserProfileResponse getProfileById(Long id);

    UserProfileResponse getProfileByUserId(Long userId);

    UserProfileResponse createProfile(UserProfileRequest profile);

    List<UserProfileResponse> getAll();

}