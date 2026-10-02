package com.owsm.AuthService.dto;

import java.util.List;

/** Payload used to replace the create / edit / delete grants of a role. */
public record RoleActionGrantRequest(List<MenuActionGrant> grants) {
}
