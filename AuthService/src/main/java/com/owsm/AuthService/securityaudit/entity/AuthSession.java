package com.owsm.AuthService.securityaudit.entity;

import com.owsm.AuthService.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_session", uniqueConstraints = @UniqueConstraint(name = "uq_auth_session_token_id", columnNames = "token_id"))
public class AuthSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "token_id", nullable = false, unique = true, updatable = false)
    private UUID tokenId;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, columnDefinition = "timestamptz")
    private Instant expiresAt;

    @Column(name = "revoked_at", columnDefinition = "timestamptz")
    private Instant revokedAt;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;
}
