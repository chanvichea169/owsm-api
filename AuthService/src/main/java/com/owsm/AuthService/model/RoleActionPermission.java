package com.owsm.AuthService.model;

import com.owsm.AuthService.enumeration.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Grants a single create / edit / delete action on a page (menu) to a role.
 * "View" access is handled separately by {@link RoleMenuConfiguration}.
 */
@Entity
@Table(
    name = "tbl_role_action_permissions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_role_action_permission",
        columnNames = {"role_name", "menu_key", "permission_action"}
    )
)
public class RoleActionPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 32)
    private RoleName roleName;

    @Column(name = "menu_key", nullable = false, length = 64)
    private String menuKey;

    @Column(name = "permission_action", nullable = false, length = 16)
    private String action;

    protected RoleActionPermission() {
    }

    public RoleActionPermission(RoleName roleName, String menuKey, String action) {
        this.roleName = roleName;
        this.menuKey = menuKey;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public String getAction() {
        return action;
    }
}
