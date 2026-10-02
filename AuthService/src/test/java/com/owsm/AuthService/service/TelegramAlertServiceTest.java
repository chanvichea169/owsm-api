package com.owsm.AuthService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramAlertServiceTest {

    @Mock
    private TelegramBotClient telegramBotClient;

    @InjectMocks
    private TelegramAlertService service;

    @BeforeEach
    void configureChatId() {
        ReflectionTestUtils.setField(service, "alertChatId", "-1004465489324");
    }

    @Test
    void sendsAlertToConfiguredGroup() {
        when(telegramBotClient.isConfigured()).thenReturn(true);

        service.destructiveAction("User deleted", "target: vichea", "admin@owsm");

        verify(telegramBotClient).sendMessage(
                eq("-1004465489324"),
                contains("User deleted"));
    }

    @Test
    void skipsDeliveryWhenChatIdIsBlank() {
        ReflectionTestUtils.setField(service, "alertChatId", "   ");

        service.send("should not be sent");

        verify(telegramBotClient, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void skipsDeliveryWhenBotTokenMissing() {
        when(telegramBotClient.isConfigured()).thenReturn(false);

        service.send("should not be sent");

        verify(telegramBotClient, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void deliveryFailureIsSwallowed() {
        when(telegramBotClient.isConfigured()).thenReturn(true);
        doThrow(new IllegalStateException("TELEGRAM_API_REQUEST_FAILED"))
                .when(telegramBotClient).sendMessage(anyString(), anyString());

        service.securityEvent("Suspicious login detected", "user: vichea", "vichea");

        verify(telegramBotClient).sendMessage(eq("-1004465489324"), contains("Suspicious login"));
    }
}
