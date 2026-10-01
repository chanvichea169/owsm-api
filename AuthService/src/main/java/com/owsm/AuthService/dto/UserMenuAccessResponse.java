package com.owsm.AuthService.dto;

import java.util.List;

public record UserMenuAccessResponse(
    Long userId,
    List<String> menuKeys,
    boolean custom
) {
}
