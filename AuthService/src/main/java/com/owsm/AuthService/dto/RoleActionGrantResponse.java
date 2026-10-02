package com.owsm.AuthService.dto;

import java.util.List;

/** Create / edit / delete grants for every page a role can act on. */
public record RoleActionGrantResponse(String roleName, List<MenuActionGrant> grants) {
}
