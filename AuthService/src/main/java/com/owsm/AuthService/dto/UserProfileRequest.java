package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    private LocalDate birthDate;

    private Long userId;
    private UserIdResponse user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserIdResponse {
        private Long id;
    }
}
