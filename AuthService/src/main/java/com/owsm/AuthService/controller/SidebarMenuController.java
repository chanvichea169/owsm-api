package com.owsm.AuthService.controller;

import com.owsm.AuthService.dto.SidebarMenuRequest;
import com.owsm.AuthService.dto.SidebarMenuOrderRequest;
import com.owsm.AuthService.dto.SidebarMenuResponse;
import com.owsm.AuthService.service.SidebarMenuService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sidebar-menus")
public class SidebarMenuController {

    private final SidebarMenuService sidebarMenuService;

    public SidebarMenuController(SidebarMenuService sidebarMenuService) {
        this.sidebarMenuService = sidebarMenuService;
    }

    @GetMapping
    public List<SidebarMenuResponse> getMenus() {
        return sidebarMenuService.getEnabledMenus();
    }

    @PostMapping
    public SidebarMenuResponse createMenu(@Valid @RequestBody SidebarMenuRequest request) {
        return sidebarMenuService.createMenu(request);
    }

    @PutMapping("/{menuKey}/order")
    public SidebarMenuResponse updateMenuOrder(
        @PathVariable String menuKey,
        @Valid @RequestBody SidebarMenuOrderRequest request
    ) {
        return sidebarMenuService.updateMenuOrder(menuKey, request.sortOrder());
    }
}
