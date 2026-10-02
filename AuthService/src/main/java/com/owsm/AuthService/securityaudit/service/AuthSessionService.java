package com.owsm.AuthService.securityaudit.service;

import com.owsm.AuthService.model.User;
import com.owsm.AuthService.securityaudit.entity.AuthSession;
import com.owsm.AuthService.securityaudit.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthSessionService {
    private final AuthSessionRepository authSessionRepository;
    private final ClientRequestInfoExtractor requestInfoExtractor;
    private final long tokenExpirationMillis;

    public AuthSessionService(
            AuthSessionRepository authSessionRepository,
            ClientRequestInfoExtractor requestInfoExtractor,
            @Value("${jwt.expiration}") long tokenExpirationMillis) {
        this.authSessionRepository = authSessionRepository;
        this.requestInfoExtractor = requestInfoExtractor;
        this.tokenExpirationMillis = tokenExpirationMillis;
    }

    @Transactional
    public SessionIdentifiers create(User user) {
        Instant createdAt = Instant.now();
        UUID tokenId = UUID.randomUUID();
        AuthSession session = new AuthSession();
        session.setTokenId(tokenId);
        session.setUser(user);
        session.setCreatedAt(createdAt);
        session.setExpiresAt(createdAt.plusMillis(tokenExpirationMillis));
        session.setIpAddress(requestInfoExtractor.currentRequest().ipAddress());
        AuthSession persistedSession = authSessionRepository.save(session);
        return new SessionIdentifiers(
                persistedSession.getSessionId(),
                tokenId,
                persistedSession.getExpiresAt());
    }

    @Transactional
    public boolean revoke(UUID sessionId, Long userId) {
        Optional<AuthSession> session = authSessionRepository.findBySessionIdAndUser_Id(sessionId, userId);
        if (session.isEmpty() || session.get().getRevokedAt() != null) {
            return false;
        }
        session.get().setRevokedAt(Instant.now());
        authSessionRepository.save(session.get());
        return true;
    }

    @Transactional
    public int revokeAll(Long userId) {
        return authSessionRepository.revokeAllActiveByUserId(userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId, UUID tokenId, Instant now) {
        return authSessionRepository
                .findBySessionIdAndTokenIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, tokenId, now)
                .isPresent();
    }

    public record SessionIdentifiers(UUID sessionId, UUID tokenId, Instant expiresAt) {
    }
}
