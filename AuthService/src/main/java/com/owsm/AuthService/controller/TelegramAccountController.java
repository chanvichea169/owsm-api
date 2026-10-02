package com.owsm.AuthService.controller;

import com.owsm.AuthService.model.User;
import com.owsm.AuthService.repository.UserRepository;
import com.owsm.AuthService.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/telegram")
public class TelegramAccountController {
    private final TelegramLinkService telegramLinkService;
    private final UserRepository userRepository;

    @GetMapping("/status")
    public LinkStatus status() {
        return new LinkStatus(getAuthenticatedUser().getTelegramChatId() != null);
    }

    @PostMapping("/link")
    public TelegramLinkService.LinkStart startLink() {
        return telegramLinkService.startLink(getAuthenticatedUser());
    }

    @DeleteMapping("/link")
    public ResponseEntity<Void> unlink() {
        telegramLinkService.unlink(getAuthenticatedUser());
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(authentication.getName())
                .or(() -> userRepository.findByUsername(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public record LinkStatus(boolean linked) {
    }
}