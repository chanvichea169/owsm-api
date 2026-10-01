package com.owsm.AuthService.securityaudit.entity;

import com.owsm.AuthService.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
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
@Table(name = "auth_device", uniqueConstraints = @UniqueConstraint(name = "uq_auth_device_user_device", columnNames = {"user_id", "device_id"}))
public class AuthDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "display_name", length = 128)
    private String displayName;

    @Column(name = "display_name_en", length = 128)
    private String displayNameEn;

    @Column(name = "display_name_kh", length = 128)
    private String displayNameKh;

    @Column(name = "device_type", length = 64)
    private String deviceType;

    @Column(length = 128)
    private String platform;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "last_ip_address", columnDefinition = "inet")
    private InetAddress lastIpAddress;

    @Column(nullable = false)
    @Builder.Default
    private boolean trusted = false;

    @Column(name = "last_seen_at", columnDefinition = "timestamptz")
    private Instant lastSeenAt;

    @Column(name = "revoked_at", columnDefinition = "timestamptz")
    private Instant revokedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;
}
