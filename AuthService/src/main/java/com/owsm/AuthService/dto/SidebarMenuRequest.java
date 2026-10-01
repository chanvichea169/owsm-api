package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SidebarMenuRequest(
    @NotBlank @Size(max = 120) String labelEn,
    @Size(max = 120) String labelKm,
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^/(?!/)[^?#]*$")
    String path,
    @NotBlank @Size(max = 32) String icon,
    @Size(max = 64) String parentKey,
    @Min(0) Integer sortOrder
) {
}
