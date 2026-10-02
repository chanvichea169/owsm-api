package com.owsm.AuthService.service;

import com.owsm.AuthService.dto.NotificationResponse;
import com.owsm.AuthService.model.NotificationType;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.UserNotification;
import com.owsm.AuthService.repository.UserNotificationRepository;
import com.owsm.AuthService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TelegramBotClient telegramBotClient;

    public List<NotificationResponse> listForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllReadByRecipientId(userId, Instant.now());
    }

    @Transactional
    public NotificationResponse create(
            Long userId,
            String title,
            String message,
            NotificationType type
    ) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserNotification notification = new UserNotification();
        notification.setRecipient(recipient);
        notification.setTitle(title.trim());
        notification.setMessage(message.trim());
        notification.setType(type == null ? NotificationType.INFO : type);
        notification.setCreatedAt(Instant.now());
        UserNotification saved = notificationRepository.save(notification);
        deliverToTelegram(recipient, saved);
        return toResponse(saved);
    }

    /**
     * Pushes the notification to the recipient's private Telegram chat when the
     * account is linked and the bot token is configured. Delivery failures are
     * swallowed so they never break notification creation.
     */
    private void deliverToTelegram(User recipient, UserNotification notification) {
        String chatId = recipient.getTelegramChatId();
        if (chatId == null || chatId.isBlank() || !telegramBotClient.isConfigured()) {
            return;
        }
        try {
            telegramBotClient.sendMessage(
                    chatId,
                    notification.getTitle() + "\n\n" + notification.getMessage()
            );
        } catch (RuntimeException exception) {
            /* Ignore — the in-app notification is already stored. */
        }
    }

    private NotificationResponse toResponse(UserNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name().toLowerCase(Locale.ROOT),
                notification.getCreatedAt(),
                notification.getReadAt() != null
        );
    }
}