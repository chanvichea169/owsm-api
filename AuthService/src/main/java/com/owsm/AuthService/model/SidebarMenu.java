package com.owsm.AuthService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_sidebar_menus")
public class SidebarMenu {

    @Id
    @Column(name = "menu_key", nullable = false, length = 64)
    private String menuKey;

    @Column(name = "label_en", nullable = false, length = 120)
    private String labelEn;

    @Column(name = "label_km", length = 120)
    private String labelKm;

    @Column(name = "route_path", length = 255)
    private String path;

    @Column(name = "icon_name", nullable = false, length = 32)
    private String icon;

    @Column(name = "parent_key", length = 64)
    private String parentKey;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected SidebarMenu() {
    }

    public SidebarMenu(
        String menuKey,
        String labelEn,
        String labelKm,
        String path,
        String icon,
        String parentKey,
        int sortOrder,
        boolean enabled
    ) {
        this.menuKey = menuKey;
        this.labelEn = labelEn;
        this.labelKm = labelKm;
        this.path = path;
        this.icon = icon;
        this.parentKey = parentKey;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public String getLabelKm() {
        return labelKm;
    }

    public String getPath() {
        return path;
    }

    public String getIcon() {
        return icon;
    }

    public String getParentKey() {
        return parentKey;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
