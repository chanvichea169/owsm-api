package com.owsm.AuthService.service;

import com.owsm.AuthService.model.TelegramLinkToken;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.TelegramLinkTokenRepository;
import com.owsm.AuthService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TelegramLinkService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TelegramLinkTokenRepository linkTokenRepository;
    private final UserRepository userRepository;
    private final TelegramBotClient telegramBotClient;

    @Value("${telegram.bot.link-expiration-minutes:10}")
    private long linkExpirationMinutes;

    @Transactional
    public LinkStart startLink(User user) {
        if (!telegramBotClient.isConfigured()) {
            throw new IllegalStateException("TELEGRAM_NOT_CONFIGURED");
        }

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plus(linkExpirationMinutes, ChronoUnit.MINUTES);

        linkTokenRepository.deleteAllByUserId(user.getId());
        TelegramLinkToken linkToken = new TelegramLinkToken();
        linkToken.setUser(user);
        linkToken.setTokenHash(hash(token));
        linkToken.setExpiresAt(expiresAt);
        linkTokenRepository.save(linkToken);

        String botUsername = telegramBotClient.getBotUsername();
        return new LinkStart("https://t.me/" + botUsername + "?start=" + token, expiresAt);
    }

    @Transactional
    public boolean completeLink(String token, String chatId) {
        if (isBlank(token) || isBlank(chatId)) return false;

        TelegramLinkToken linkToken = linkTokenRepository
                .findActiveByTokenHash(hash(token), Instant.now())
                .orElse(null);
        if (linkToken == null) return false;

        User user = userRepository.findById(linkToken.getUser().getId()).orElse(null);
        if (user == null || userRepository.existsByTelegramChatIdAndIdNot(chatId, user.getId())) {
            return false;
        }

        linkToken.setUsedAt(Instant.now());
        linkTokenRepository.save(linkToken);
        user.setTelegramChatId(chatId);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void unlink(User user) {
        user.setTelegramChatId(null);
        userRepository.save(user);
        linkTokenRepository.deleteAllByUserId(user.getId());
    }

    public record LinkStart(String url, Instant expiresAt) {
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}