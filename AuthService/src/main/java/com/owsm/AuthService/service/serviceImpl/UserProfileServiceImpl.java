package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.dto.UserProfileRequest;
import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.UserProfile;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.repository.UserProfileRepository;
import com.owsm.AuthService.service.UserProfileService;
import com.owsm.AuthService.service.handler.UserProfileServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private UserProfileServiceHandler handler;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public UserProfileResponse getProfileById(Long id) {
        UserProfile entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return handler.convertToResponse(entity);
    }

    @Override
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user id: " + userId));
        return handler.convertToResponse(entity);
    }

    @Override
    public UserProfileResponse createProfile(UserProfileRequest profile, MultipartFile avatar) {
        UserProfile entity = handler.convertToEntity(profile);
        Long userId = extractUserId(profile);

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));
            entity.setUser(user);
        }

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String originalName = avatar.getOriginalFilename() == null
                        ? "avatar"
                        : Paths.get(avatar.getOriginalFilename()).getFileName().toString();
                String fileName = UUID.randomUUID() + "_" + originalName;
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                Files.copy(avatar.getInputStream(), uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                entity.setAvatarUrl(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store avatar image", e);
            }
        }

        UserProfile savedProfile = repository.save(entity);
        return handler.convertToResponse(savedProfile);
    }

    private Long extractUserId(UserProfileRequest profile) {
        if (profile.getUserId() != null) {
            return profile.getUserId();
        }

        if (profile.getUser() != null) {
            return profile.getUser().getId();
        }

        return null;
    }
    @Override
    public List<UserProfileResponse> getAll() {
        List<UserProfile> entities = repository.findAll();
        return handler.convertToResponseList(entities);
    }

    @Override
    public UserProfileResponse updateProfile(Long id, UserProfileRequest request, MultipartFile profileImage) {
        UserProfile existingProfile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        existingProfile.setFirstName(request.getFirstName());
        existingProfile.setLastName(request.getLastName());
        existingProfile.setPhoneNumber(request.getPhoneNumber());
        existingProfile.setAddress(request.getAddress());
        existingProfile.setBio(request.getBio());
        if (request.getBirthDate() != null) {
            existingProfile.setBirthDate(
                    Date.from(request.getBirthDate().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())
            );
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String originalName = profileImage.getOriginalFilename() == null
                        ? "avatar"
                        : Paths.get(profileImage.getOriginalFilename()).getFileName().toString();
                String fileName = UUID.randomUUID() + "_" + originalName;
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                Files.copy(profileImage.getInputStream(), uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                existingProfile.setAvatarUrl(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store profile image", e);
            }
        }

        UserProfile updatedProfile = repository.save(existingProfile);
        return handler.convertToResponse(updatedProfile);
    }
}
