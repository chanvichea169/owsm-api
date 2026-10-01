package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.SidebarMenuRequest;
import com.owsm.AuthService.dto.SidebarMenuResponse;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SidebarMenuService {

    private static final Set<String> AVAILABLE_ICONS = Set.of(
        "dashboard",
        "news",
        "reports",
        "settings",
        "users",
        "building",
        "map-pin",
        "shield",
        "list",
        "video",
        "music",
        "folder",
        "calendar"
    );
    private static final Pattern NON_KEY_CHARACTERS = Pattern.compile("[^a-z0-9]+");

    private final SidebarMenuRepository menuRepository;

    public SidebarMenuService(SidebarMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public List<SidebarMenuResponse> getEnabledMenus() {
        return menuRepository.findAllByEnabledTrueOrderBySortOrderAsc()
            .stream()
            .map(this::response)
            .toList();
    }

    @Transactional
    public SidebarMenuResponse createMenu(SidebarMenuRequest request) {
        String icon = request.icon().trim().toLowerCase(Locale.ROOT);
        if (!AVAILABLE_ICONS.contains(icon)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The selected sidebar icon is not supported"
            );
        }

        String parentKey = request.parentKey() == null || request.parentKey().isBlank()
            ? null
            : request.parentKey().trim();
        if (parentKey != null) {
            SidebarMenu parent = menuRepository.findById(parentKey)
                .filter(SidebarMenu::isEnabled)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected parent menu does not exist"
                ));
            if (parent.getPath() != null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A menu item can only be nested under a menu group"
                );
            }
        }

        String menuKey = uniqueKey(request.labelEn());
        int sortOrder = request.sortOrder() != null
            ? request.sortOrder()
            : menuRepository.findAllByEnabledTrueOrderBySortOrderAsc()
                .stream()
                .mapToInt(SidebarMenu::getSortOrder)
                .max()
                .orElse(-1) + 1;
        SidebarMenu menu = new SidebarMenu(
            menuKey,
            request.labelEn().trim(),
            request.labelKm() == null || request.labelKm().isBlank()
                ? null
                : request.labelKm().trim(),
            request.path().trim(),
            icon,
            parentKey,
            sortOrder,
            true
        );
        return response(menuRepository.save(menu));
    }

    @Transactional
    public SidebarMenuResponse updateMenuOrder(String menuKey, int sortOrder) {
        SidebarMenu menu = menuRepository.findById(menuKey)
            .filter(SidebarMenu::isEnabled)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Sidebar menu item not found"
            ));
        menu.setSortOrder(sortOrder);
        return response(menuRepository.save(menu));
    }

    private String uniqueKey(String label) {
        String normalized = Normalizer.normalize(label, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
        String baseKey = NON_KEY_CHARACTERS.matcher(normalized)
            .replaceAll("-")
            .replaceAll("^-|-$", "");
        if (baseKey.isBlank()) {
            baseKey = "menu";
        }
        baseKey = baseKey.substring(0, Math.min(baseKey.length(), 56));

        String candidate = baseKey;
        int suffix = 2;
        while (menuRepository.existsByMenuKey(candidate)) {
            String suffixText = "-" + suffix++;
            candidate = baseKey.substring(0, Math.min(baseKey.length(), 64 - suffixText.length()))
                + suffixText;
        }
        return candidate;
    }

    private SidebarMenuResponse response(SidebarMenu menu) {
        return new SidebarMenuResponse(
            menu.getMenuKey(),
            menu.getLabelEn(),
            menu.getLabelKm(),
            menu.getPath(),
            menu.getIcon(),
            menu.getParentKey(),
            menu.getSortOrder(),
            menu.isEnabled()
        );
    }
}
