package com.owsm.AuthService.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Receives Telegram updates through long polling instead of a public webhook.
 *
 * <p>This is what makes the integration work with just a bot token — no
 * Cloudflare tunnel, domain, or any other public HTTPS endpoint is required.
 * The bot's private chat is linked by polling {@code getUpdates} for the
 * {@code /start &lt;code&gt;} message produced when a user opens their personal
 * {@code https://t.me/<bot>?start=<code>} link.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramPollingService {

    private final TelegramBotClient telegramBotClient;
    private final TelegramLinkService telegramLinkService;

    private final AtomicLong offset = new AtomicLong(0L);

    @PostConstruct
    void enablePolling() {
        if (!telegramBotClient.isConfigured()) {
            log.info("Telegram bot token is not configured; Telegram linking and notifications are disabled");
            return;
        }
        try {
            /* Ensure no webhook is registered — Telegram forbids getUpdates
               while a webhook is active. */
            telegramBotClient.deleteWebhook();
            log.info("Telegram bot polling enabled (no public webhook or tunnel required)");
        } catch (RuntimeException exception) {
            log.warn("Unable to reset the Telegram webhook; polling may not receive updates");
        }
    }

    @Scheduled(fixedDelayString = "${telegram.bot.poll-interval-ms:3000}")
    public void poll() {
        if (!telegramBotClient.isConfigured()) {
            return;
        }

        List<Map<String, Object>> updates;
        try {
            updates = telegramBotClient.getUpdates(offset.get());
        } catch (RuntimeException exception) {
            /* Transient network/API failure — retry on the next tick. */
            return;
        }

        for (Map<String, Object> update : updates) {
            advanceOffset(update);
            try {
                handleUpdate(update);
            } catch (RuntimeException exception) {
                log.warn("Unable to process a Telegram update");
            }
        }
    }

    private void advanceOffset(Map<String, Object> update) {
        Object updateId = update.get("update_id");
        if (updateId instanceof Number number) {
            offset.accumulateAndGet(number.longValue() + 1L, Math::max);
        }
    }

    private void handleUpdate(Map<String, Object> update) {
        if (!(update.get("message") instanceof Map<?, ?> message)) {
            return;
        }
        if (!(message.get("chat") instanceof Map<?, ?> chat)
                || !"private".equals(chat.get("type"))) {
            return;
        }

        String chatId = asText(chat.get("id"));
        if (chatId.isBlank()) {
            return;
        }

        String text = asText(message.get("text")).trim();
        if (text.isEmpty()) {
            return;
        }

        String[] command = text.split("\\s+", 2);
        if (command.length == 2 && command[0].matches("/start(?:@\\w+)?")) {
            boolean linked;
            try {
                linked = telegramLinkService.completeLink(command[1], chatId);
            } catch (RuntimeException exception) {
                log.warn("Unable to complete Telegram account linking");
                linked = false;
            }
            telegramBotClient.sendMessage(
                    chatId,
                    linked
                            ? "Telegram is linked to your OWSM account. You will now receive notifications here."
                            : "This link is invalid, expired, or already connected to another account. Start again from your OWSM profile."
            );
            return;
        }

        telegramBotClient.sendMessage(
                chatId,
                "Use the secure link from your OWSM profile to connect this account."
        );
    }

    private static String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}