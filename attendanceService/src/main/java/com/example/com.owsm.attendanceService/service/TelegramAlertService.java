package com.example.attendanceService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Sends operational alerts to the shared OWSM Telegram group so administrators
 * are notified when a destructive action happens in the attendance service.
 *
 * <p>Delivery is fire-and-forget: failures are logged and never propagated, so
 * alerting can never break the business operation that triggered it.</p>
 */
@Service
public class TelegramAlertService {

    private static final Logger log = LoggerFactory.getLogger(TelegramAlertService.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient client;
    private final String alertChatId;

    public TelegramAlertService(
        @Value("${telegram.bot.token:}") String botToken,
        @Value("${telegram.bot.alert-chat-id:}") String alertChatId
    ) {
        this.alertChatId = alertChatId;
        this.client = (botToken == null || botToken.isBlank())
            ? null
            : RestClient.builder().baseUrl("https://api.telegram.org/bot" + botToken + "/").build();
    }

    public boolean isEnabled() {
        return client != null && alertChatId != null && !alertChatId.isBlank();
    }

    /** Fire-and-forget send; never throws. */
    public void send(String text) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return;
        }
        try {
            client.post()
                .uri("sendMessage")
                .body(Map.of("chat_id", alertChatId.trim(), "text", text))
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("Unable to deliver a Telegram alert to the configured group");
        }
    }

    /** Alerts the group about a destructive action. */
    public void destructiveAction(String entity, String detail, String actor) {
        send(build("\uD83D\uDDD1\uFE0F " + entity, detail, actor));
    }

    private static String build(String title, String detail, String actor) {
        StringBuilder message = new StringBuilder(title);
        if (detail != null && !detail.isBlank()) {
            for (String line : detail.split("\n")) {
                if (!line.isBlank()) {
                    message.append('\n').append("\u2022 ").append(line.trim());
                }
            }
        }
        message.append('\n').append("\u2022 by: ")
            .append(actor == null || actor.isBlank() ? "unknown" : actor);
        message.append('\n').append("\u2022 at: ")
            .append(LocalDateTime.now().format(TIMESTAMP));
        return message.toString();
    }
}
