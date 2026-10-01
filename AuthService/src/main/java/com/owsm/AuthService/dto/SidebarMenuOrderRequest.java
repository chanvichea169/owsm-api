package com.owsm.AuthService.dto;

import jakarta.validation.constraints.Min;

public record SidebarMenuOrderRequest(@Min(0) int sortOrder) {
}
