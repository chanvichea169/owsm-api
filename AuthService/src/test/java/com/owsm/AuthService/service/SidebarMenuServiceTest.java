package com.owsm.AuthService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.owsm.AuthService.dto.SidebarMenuRequest;
import com.owsm.AuthService.model.SidebarMenu;
import com.owsm.AuthService.repository.SidebarMenuRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SidebarMenuServiceTest {

    @Mock
    private SidebarMenuRepository menuRepository;

    @InjectMocks
    private SidebarMenuService service;

    @Test
    void createsMenuItemsWithGeneratedKeysAndIncrementedOrder() {
        when(menuRepository.existsByMenuKey("quality-reports")).thenReturn(false);
        when(menuRepository.findAllByEnabledTrueOrderBySortOrderAsc())
            .thenReturn(List.of(
                new SidebarMenu(
                    "dashboard",
                    "app.nav.dashboard",
                    null,
                    "/",
                    "dashboard",
                    null,
                    10,
                    true
                )
            ));
        when(menuRepository.save(any(SidebarMenu.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.createMenu(
            new SidebarMenuRequest(
                "Quality Reports",
                "របាយការណ៍គុណភាព",
                "/reports",
                "reports",
                null,
                null
            )
        );

        assertEquals("quality-reports", created.key());
        assertEquals("/reports", created.path());
        assertEquals("របាយការណ៍គុណភាព", created.labelKm());
        assertEquals(11, created.sortOrder());
    }

    @Test
    void usesRequestedOrderWhenCreatingMenu() {
        when(menuRepository.existsByMenuKey("quality-reports")).thenReturn(false);
        when(menuRepository.save(any(SidebarMenu.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.createMenu(
            new SidebarMenuRequest(
                "Quality Reports",
                null,
                "/reports",
                "reports",
                null,
                3
            )
        );

        assertEquals(3, created.sortOrder());
    }

    @Test
    void updatesExistingMenuOrder() {
        SidebarMenu menu = new SidebarMenu(
            "quality-reports",
            "Quality Reports",
            null,
            "/reports",
            "reports",
            null,
            11,
            true
        );
        when(menuRepository.findById("quality-reports"))
            .thenReturn(Optional.of(menu));
        when(menuRepository.save(menu)).thenReturn(menu);

        var updated = service.updateMenuOrder("quality-reports", 2);

        verify(menuRepository).save(menu);
        assertEquals(2, updated.sortOrder());
    }
}
