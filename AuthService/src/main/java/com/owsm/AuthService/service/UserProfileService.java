package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserProfileService {
    UserProfileResponse getProfileById(Long id);

    UserProfileResponse createProfile(UserProfileRequest profile);
}
