package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserProfileResponse> createProfile(
            @ModelAttribute UserProfileRequest formRequest,
            @RequestPart(value = "request", required = false) UserProfileRequest requestPart,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "avatarUrl", required = false) MultipartFile avatarUrlFile,
            @RequestPart(value = "image", required = false) MultipartFile image,
            MultipartHttpServletRequest multipartRequest
    ) {
        UserProfileRequest request = requestPart != null ? requestPart : formRequest;
        MultipartFile profileImage = resolveProfileImage(
                avatar, file, avatarUrlFile, image, multipartRequest
        );

        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Profile request payload is required");
        }

        UserProfileResponse response = profileService.createProfile(request, profileImage);
        return ResponseEntity.ok(response);
    }

    private MultipartFile resolveProfileImage(
            MultipartFile avatar,
            MultipartFile file,
            MultipartFile avatarUrlFile,
            MultipartFile image,
            MultipartHttpServletRequest multipartRequest
    ) {
        if (avatar != null && !avatar.isEmpty()) return avatar;
        if (file != null && !file.isEmpty()) return file;
        if (avatarUrlFile != null && !avatarUrlFile.isEmpty()) return avatarUrlFile;
        if (image != null && !image.isEmpty()) return image;

        Iterator<String> fileNames = multipartRequest.getFileNames();
        while (fileNames.hasNext()) {
            MultipartFile candidate = multipartRequest.getFile(fileNames.next());
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }

        return null;
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfileByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long id,
            @ModelAttribute UserProfileRequest formRequest,
            @RequestPart(value = "request", required = false) UserProfileRequest requestPart,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "avatarUrl", required = false) MultipartFile avatarUrlFile,
            @RequestPart(value = "image", required = false) MultipartFile image,
            MultipartHttpServletRequest multipartRequest
    ) {
        UserProfileRequest request = requestPart != null ? requestPart : formRequest;
        MultipartFile profileImage = resolveProfileImage(
                avatar, file, avatarUrlFile, image, multipartRequest
        );

        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Profile request payload is required");
        }

        UserProfileResponse response = profileService.updateProfile(id, request, profileImage);
        return ResponseEntity.ok(response);
    }
}
