package com.owsm.AuthService.dto;

public record SidebarMenuResponse(
    String key,
    String labelEn,
    String labelKm,
    String path,
    String icon,
    String parentKey,
    int sortOrder,
    boolean enabled
) {
}
