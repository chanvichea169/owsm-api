package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.SidebarMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SidebarMenuRepository extends JpaRepository<SidebarMenu, String> {

    List<SidebarMenu> findAllByEnabledTrueOrderBySortOrderAsc();

    boolean existsByMenuKey(String menuKey);
}
