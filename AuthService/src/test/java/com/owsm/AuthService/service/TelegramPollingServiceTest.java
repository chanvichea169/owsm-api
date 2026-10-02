package com.owsm.AuthService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that incoming Telegram updates make the bot reply with a message.
 * These tests cover the "send message with the bot" behaviour without needing
 * a real bot token or network access.
 */
@ExtendWith(MockitoExtension.class)
class TelegramPollingServiceTest {

    @Mock
    private TelegramBotClient telegramBotClient;

    @Mock
    private TelegramLinkService telegramLinkService;

    @InjectMocks
    private TelegramPollingService service;

    @Test
    void repliesWithLinkConfirmationWhenStartTokenIsValid() {
        Map<String, Object> update = privateUpdate(1L, 987654L, "/start one-time-code");

        when(telegramBotClient.isConfigured()).thenReturn(true);
        when(telegramBotClient.getUpdates(0L)).thenReturn(List.of(update));
        when(telegramLinkService.completeLink("one-time-code", "987654")).thenReturn(true);

        service.poll();

        verify(telegramLinkService).completeLink("one-time-code", "987654");
        verify(telegramBotClient).sendMessage(eq("987654"), contains("linked"));
    }

    @Test
    void repliesWithLinkFailureWhenStartTokenIsInvalid() {
        Map<String, Object> update = privateUpdate(2L, 987654L, "/start expired-code");

        when(telegramBotClient.isConfigured()).thenReturn(true);
        when(telegramBotClient.getUpdates(0L)).thenReturn(List.of(update));
        when(telegramLinkService.completeLink("expired-code", "987654")).thenReturn(false);

        service.poll();

        verify(telegramBotClient).sendMessage(eq("987654"), contains("invalid"));
    }

    @Test
    void repliesWithGuidanceForPlainTextMessage() {
        Map<String, Object> update = privateUpdate(3L, 42L, "hello there");

        when(telegramBotClient.isConfigured()).thenReturn(true);
        when(telegramBotClient.getUpdates(0L)).thenReturn(List.of(update));

        service.poll();

        verify(telegramLinkService, never()).completeLink(anyString(), anyString());
        verify(telegramBotClient).sendMessage(eq("42"), contains("secure link"));
    }

    @Test
    void ignoresNonPrivateChats() {
        Map<String, Object> chat = Map.of("id", 777L, "type", "group");
        Map<String, Object> message = Map.of("chat", chat, "text", "/start one-time-code");
        Map<String, Object> update = Map.of("update_id", 4L, "message", message);

        when(telegramBotClient.isConfigured()).thenReturn(true);
        when(telegramBotClient.getUpdates(0L)).thenReturn(List.of(update));

        service.poll();

        verify(telegramLinkService, never()).completeLink(anyString(), anyString());
        verify(telegramBotClient, never()).sendMessage(anyString(), anyString());
    }

    private static Map<String, Object> privateUpdate(long updateId, long chatId, String text) {
        Map<String, Object> chat = Map.of("id", chatId, "type", "private");
        Map<String, Object> message = Map.of("chat", chat, "text", text);
        return Map.of("update_id", updateId, "message", message);
    }
}
