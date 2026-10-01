package com.owsm.AuthService.securityaudit.entity;

import com.owsm.AuthService.model.User;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventStatus;
import com.owsm.AuthService.securityaudit.enumeration.SecurityEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "security_audit")
public class SecurityAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 48)
    private SecurityEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 24)
    private SecurityEventStatus status;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "text")
    private String descriptionKh;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "forwarded_ip", columnDefinition = "inet")
    private InetAddress forwardedIp;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private AuthDevice device;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = Map.of();
}
