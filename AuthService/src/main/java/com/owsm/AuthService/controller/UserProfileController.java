package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> createProfile(@RequestBody UserProfileRequest request) {
        UserProfileResponse response = profileService.createProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfileByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }
}
