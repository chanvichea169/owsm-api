package com.owsm.AuthService.model;

import com.owsm.AuthService.enumeration.RoleName;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_role_menu_configurations")
public class RoleMenuConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 32, unique = true)
    private RoleName roleName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tbl_role_menu_items",   
        joinColumns = @JoinColumn(name = "configuration_id")
    )
    @Column(name = "menu_key", nullable = false, length = 64)
    private Set<String> menuKeys = new HashSet<>();

    protected RoleMenuConfiguration() {
    }

    public RoleMenuConfiguration(RoleName roleName, Set<String> menuKeys) {
        this.roleName = roleName;
        this.menuKeys = new HashSet<>(menuKeys);
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public Set<String> getMenuKeys() {
        return menuKeys;
    }

    public void setMenuKeys(Set<String> menuKeys) {
        this.menuKeys = new HashSet<>(menuKeys);
    }
}
