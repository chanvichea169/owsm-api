package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/profile")
public class UserProfileController {

    @Qualifier("userProfileServiceImpl")
    @Autowired
    private UserProfileService profileService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> createProfile(@RequestBody UserProfileRequest request) {
        UserProfileResponse response = profileService.createProfile(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }
}