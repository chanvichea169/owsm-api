package com.owsm.AuthService.service;

import com.owsm.AuthService.model.UserNotification;
import com.owsm.AuthService.repository.UserNotificationRepository;
import com.owsm.AuthService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private UserNotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void listsNotificationsOnlyForRequestedUser() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(17L))
                .thenReturn(List.of());

        assertEquals(List.of(), service.listForUser(17L));
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(17L);
    }

    @Test
    void cannotMarkNotificationOwnedByAnotherUserAsRead() {
        when(notificationRepository.findByIdAndRecipientId(5L, 17L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.markAsRead(5L, 17L));
        verify(notificationRepository).findByIdAndRecipientId(5L, 17L);
    }
}