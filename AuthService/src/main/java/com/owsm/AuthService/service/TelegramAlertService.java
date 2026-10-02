package com.owsm.AuthService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Delivers operational and security alerts to a configured Telegram group so
 * that administrators are notified when a destructive action happens or a
 * risky event is detected.
 *
 * <p>Delivery is fire-and-forget: failures are logged and never propagated, so
 * alerting can never break the business operation that triggered it.</p>
 */
@Service
public class TelegramAlertService {

    private static final Logger log = LoggerFactory.getLogger(TelegramAlertService.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TelegramBotClient telegramBotClient;

    @Value("${telegram.bot.alert-chat-id:}")
    private String alertChatId;

    public TelegramAlertService(TelegramBotClient telegramBotClient) {
        this.telegramBotClient = telegramBotClient;
    }

    public boolean isEnabled() {
        return alertChatId != null && !alertChatId.isBlank() && telegramBotClient.isConfigured();
    }

    /** Fire-and-forget send; never throws. */
    public void send(String text) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return;
        }
        try {
            telegramBotClient.sendMessage(alertChatId.trim(), text);
        } catch (RuntimeException exception) {
            log.warn("Unable to deliver a Telegram alert to the configured group");
        }
    }

    /**
     * Alerts the group about a destructive action, e.g.
     * <pre>🗑️ User deleted
     * • target: vichea &lt;chanvichea169@gmail.com&gt;
     * • by: admin@owsm
     * • at: 2026-10-03 01:55:00</pre>
     */
    public void destructiveAction(String entity, String detail, String actor) {
        send(build("\uD83D\uDDD1\uFE0F " + entity, detail, actor));
    }

    /** Alerts the group about a security / risk event. */
    public void securityEvent(String title, String detail, String actor) {
        send(build("\u26A0\uFE0F " + title, detail, actor));
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

    /** Best-effort identity of the signed-in user that triggered the action. */
    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "unknown";
        }
        return authentication.getName();
    }
}
