package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoleMenuAccessRequest(@NotNull List<String> menuKeys) {
}
