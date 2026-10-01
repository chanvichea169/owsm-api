package com.owsm.AuthService.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_user_menu_configurations")
public class UserMenuConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tbl_user_menu_items",
        joinColumns = @JoinColumn(name = "configuration_id")
    )
    @Column(name = "menu_key", nullable = false, length = 64)
    private Set<String> menuKeys = new HashSet<>();

    protected UserMenuConfiguration() {
    }

    public UserMenuConfiguration(Long userId, Set<String> menuKeys) {
        this.userId = userId;
        this.menuKeys = new HashSet<>(menuKeys);
    }

    public Set<String> getMenuKeys() {
        return menuKeys;
    }

    public void setMenuKeys(Set<String> menuKeys) {
        this.menuKeys = new HashSet<>(menuKeys);
    }
}
