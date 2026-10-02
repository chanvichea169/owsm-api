package com.owsm.AuthService.service;

import com.owsm.AuthService.model.TelegramLinkToken;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.TelegramLinkTokenRepository;
import com.owsm.AuthService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramLinkServiceTest {
    @Mock
    private TelegramLinkTokenRepository linkTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramBotClient telegramBotClient;

    @InjectMocks
    private TelegramLinkService service;

    @BeforeEach
    void configureLinkService() {
        ReflectionTestUtils.setField(service, "linkExpirationMinutes", 10L);
    }

    @Test
    void storesOnlyHashOfShortLivedLinkCode() {
        User user = new User();
        user.setId(17L);
        when(telegramBotClient.isConfigured()).thenReturn(true);
        when(telegramBotClient.getBotUsername()).thenReturn("owsm_test_bot");

        TelegramLinkService.LinkStart link = service.startLink(user);

        ArgumentCaptor<TelegramLinkToken> saved = ArgumentCaptor.forClass(TelegramLinkToken.class);
        verify(linkTokenRepository).save(saved.capture());
        String linkCode = link.url().substring(link.url().indexOf("?start=") + 7);
        assertEquals(64, saved.getValue().getTokenHash().length());
        assertFalse(linkCode.equals(saved.getValue().getTokenHash()));
        assertTrue(saved.getValue().getExpiresAt().isAfter(Instant.now()));
        assertTrue(link.url().startsWith("https://t.me/owsm_test_bot?start="));
    }

    @Test
    void refusesChatAlreadyLinkedToAnotherUser() {
        User user = new User();
        user.setId(17L);
        TelegramLinkToken token = new TelegramLinkToken();
        token.setUser(user);
        when(linkTokenRepository.findActiveByTokenHash(anyString(), any(Instant.class)))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(17L)).thenReturn(Optional.of(user));
        when(userRepository.existsByTelegramChatIdAndIdNot("987654", 17L))
                .thenReturn(true);

        assertFalse(service.completeLink("one-time-code", "987654"));
        assertEquals(null, token.getUsedAt());
    }

    @Test
    void linksPrivateChatAndConsumesLinkCodeOnce() {
        User user = new User();
        user.setId(17L);
        TelegramLinkToken token = new TelegramLinkToken();
        token.setUser(user);
        when(linkTokenRepository.findActiveByTokenHash(anyString(), any(Instant.class)))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(17L)).thenReturn(Optional.of(user));
        when(userRepository.existsByTelegramChatIdAndIdNot("987654", 17L))
                .thenReturn(false);

        assertTrue(service.completeLink("one-time-code", "987654"));
        assertEquals("987654", user.getTelegramChatId());
        assertNotNull(token.getUsedAt());
        verify(userRepository).save(user);
        verify(linkTokenRepository).save(token);
    }
}