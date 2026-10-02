package com.owsm.AuthService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TelegramBotClient {
    @Value("${telegram.bot.token:}")
    private String botToken;

    public boolean isConfigured() {
        return botToken != null && !botToken.isBlank();
    }

    public String getBotUsername() {
        Map<String, Object> response = client().get()
                .uri("getMe")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        Object result = response == null ? null : response.get("result");
        if (result instanceof Map<?, ?> bot && bot.get("username") instanceof String username) {
            return username;
        }
        throw new IllegalStateException("TELEGRAM_BOT_IDENTITY_UNAVAILABLE");
    }

    public void sendMessage(String chatId, String text) {
        execute("sendMessage", Map.of("chat_id", chatId, "text", text));
    }

    /**
     * Removes any previously registered webhook so the bot can receive updates
     * through {@link #getUpdates(long)} long polling. This keeps the Telegram
     * integration working without a public HTTPS endpoint or tunnel.
     */
    public void deleteWebhook() {
        execute("deleteWebhook", Map.of("drop_pending_updates", false));
    }

    /**
     * Fetches new updates through long polling. {@code offset} must be the id
     * of the last processed update plus one, so Telegram only returns unseen
     * updates.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUpdates(long offset) {
        Map<String, Object> response = execute("getUpdates", Map.of(
                "offset", offset,
                "timeout", 0,
                "allowed_updates", List.of("message")
        ));
        Object result = response.get("result");
        if (result instanceof List<?> updates) {
            return (List<Map<String, Object>>) updates;
        }
        return List.of();
    }

    private Map<String, Object> execute(String method, Map<String, Object> payload) {
        Map<String, Object> response = client().post()
                .uri(method)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            throw new IllegalStateException("TELEGRAM_API_REQUEST_FAILED");
        }
        return response;
    }

    private RestClient client() {
        if (!isConfigured()) {
            throw new IllegalStateException("TELEGRAM_NOT_CONFIGURED");
        }
        return RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + botToken + "/")
                .build();
    }
}