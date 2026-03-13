package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(convertToLocalDate(text));
            }
        });
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @ModelAttribute UserProfileRequest formRequest,
            BindingResult formBinding,
            @RequestPart(value = "request", required = false) @Valid UserProfileRequest requestPart,
            BindingResult requestBinding,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "avatarUrl", required = false) MultipartFile avatarUrlFile,
            @RequestPart(value = "image", required = false) MultipartFile image,
            MultipartHttpServletRequest multipartRequest
    ) {
        if (requestPart != null) {
            if (requestBinding.hasErrors()) {
                throw new ResponseStatusException(BAD_REQUEST, formatValidationErrors(requestBinding));
            }
        } else if (formBinding.hasErrors()) {
            throw new ResponseStatusException(BAD_REQUEST, formatValidationErrors(formBinding));
        }

        UserProfileRequest request = requestPart != null ? requestPart : formRequest;
        if (requestPart != null) {
            request = mergeUserReference(request, formRequest);
        }
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

    private UserProfileRequest mergeUserReference(
            UserProfileRequest primary,
            UserProfileRequest fallback
    ) {
        if (primary == null || fallback == null) return primary;

        if (primary.getUserId() == null && fallback.getUserId() != null) {
            primary.setUserId(fallback.getUserId());
        }

        UserProfileRequest.UserIdResponse fallbackUser = fallback.getUser();
        if (fallbackUser != null && fallbackUser.getId() != null) {
            UserProfileRequest.UserIdResponse primaryUser = primary.getUser();
            if (primaryUser == null || primaryUser.getId() == null) {
                primary.setUser(fallbackUser);
            }
        }

        return primary;
    }

    private LocalDate convertToLocalDate(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(text).toLocalDate();
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "birthDate must be a valid ISO date or datetime");
            }
        }
    }

    private String formatValidationErrors(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(error -> {
                    String message = error.getDefaultMessage();
                    return (message == null || message.isBlank()) ? error.getCode() : message;
                })
                .filter(msg -> msg != null && !msg.isBlank())
                .collect(Collectors.joining("; "));
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
