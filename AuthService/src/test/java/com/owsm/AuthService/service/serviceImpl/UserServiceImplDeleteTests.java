package com.owsm.AuthService.service.serviceImpl;

import com.owsm.AuthService.api.JwtUtil;
import com.owsm.AuthService.exception.OwsmException;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.RoleRepository;
import com.owsm.AuthService.repository.TelegramLinkTokenRepository;
import com.owsm.AuthService.repository.UserMenuConfigurationRepository;
import com.owsm.AuthService.repository.UserNotificationRepository;
import com.owsm.AuthService.repository.UserProfileRepository;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.securityaudit.repository.AuthDeviceRepository;
import com.owsm.AuthService.securityaudit.repository.AuthSessionRepository;
import com.owsm.AuthService.securityaudit.service.AuthSessionService;
import com.owsm.AuthService.securityaudit.service.AuthenticationAuditService;
import com.owsm.AuthService.service.TelegramBotClient;
import com.owsm.AuthService.service.handler.UserServiceHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplDeleteTests {

    @Mock private UserRepository userRepository;
    @Mock private UserServiceHandler userServiceHandler;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JavaMailSender mailSender;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private RoleRepository roleRepository;
    @Mock private AuthenticationAuditService authenticationAuditService;
    @Mock private AuthSessionService authSessionService;
    @Mock private TelegramBotClient telegramBotClient;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private UserMenuConfigurationRepository userMenuConfigurationRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private TelegramLinkTokenRepository telegramLinkTokenRepository;
    @Mock private AuthSessionRepository authSessionRepository;
    @Mock private AuthDeviceRepository authDeviceRepository;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void clearsDependentRowsBeforeDeletingUser() throws Exception {
        User user = new User();
        user.setId(11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));

        service.deleteUser(11L);

        verify(userProfileRepository).deleteAllByUserId(11L);
        verify(userNotificationRepository).deleteAllByRecipientId(11L);
        verify(telegramLinkTokenRepository).deleteAllByUserId(11L);
        verify(userMenuConfigurationRepository).deleteAllByUserId(11L);
        verify(authSessionRepository).deleteAllByUserId(11L);
        verify(authDeviceRepository).deleteAllByUserId(11L);
        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @Test
    void refusesToDeleteSuperAdmin() {
        assertThrows(OwsmException.class, () -> service.deleteUser(1L));
        verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any(User.class));
    }
}
