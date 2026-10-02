package com.owsm.AuthService.dto;

import java.util.List;

/** Actions granted for one page (menu) key. */
public record MenuActionGrant(String menuKey, List<String> actions) {
}
