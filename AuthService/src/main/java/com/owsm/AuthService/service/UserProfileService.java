package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

}