package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserProfileService {
    UserProfileResponse getProfileById(Long id);

    UserProfileResponse getProfileByUserId(Long userId);

    UserProfileResponse createProfile(
            UserProfileRequest request,
            MultipartFile avatar
    );

    List<UserProfileResponse> getAll();

    UserProfileResponse updateProfile(Long id, UserProfileRequest request, MultipartFile profileImage);
}