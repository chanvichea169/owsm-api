package com.owsm.AuthService.securityaudit.entity;

import com.owsm.AuthService.model.User;
import com.owsm.AuthService.securityaudit.enumeration.LoginStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "login_audit")
public class LoginAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(length = 128)
    private String username;

    @Column(length = 320)
    private String email;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "forwarded_ip", columnDefinition = "inet")
    private InetAddress forwardedIp;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 32)
    private LoginStatus status;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "failure_reason_en", columnDefinition = "text")
    private String failureReasonEn;

    @Column(name = "failure_reason_kh", columnDefinition = "text")
    private String failureReasonKh;

    @Column(name = "authentication_method", length = 64)
    private String method;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "token_id")
    private UUID tokenId;

    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    @Column(name = "device_id", length = 256)
    private String deviceId;

    @Column(name = "device_name", length = 256)
    private String deviceName;

    @Column(name = "device_type", length = 64)
    private String deviceType;

    @Column(length = 128)
    private String browser;

    @Column(name = "browser_version", length = 64)
    private String browserVersion;

    @Column(name = "operating_system", length = 128)
    private String operatingSystem;

    @Column(name = "os_version", length = 64)
    private String osVersion;

    @Column(length = 256)
    private String location;

    @Column(name = "location_en", length = 256)
    private String locationEn;

    @Column(name = "location_kh", length = 256)
    private String locationKh;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "country_name", length = 128)
    private String countryName;

    @Column(name = "country_name_en", length = 128)
    private String countryNameEn;

    @Column(name = "country_name_kh", length = 128)
    private String countryNameKh;

    @Column(length = 128)
    private String city;

    @Column(name = "city_en", length = 128)
    private String cityEn;

    @Column(name = "city_kh", length = 128)
    private String cityKh;

    @Column(length = 128)
    private String timezone;

    @Column(length = 128)
    private String application;

    @Column(length = 128)
    private String platform;

    @Column(name = "api_version", length = 64)
    private String apiVersion;

    @Column(name = "suspicious", nullable = false)
    @Builder.Default
    private boolean suspicious = false;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "mfa_required", nullable = false)
    @Builder.Default
    private boolean mfaRequired = false;

    @Column(name = "mfa_successful")
    private Boolean mfaSuccessful;

    @Column(name = "mfa_method", length = 64)
    private String mfaMethod;

    @Column(name = "mfa_at", columnDefinition = "timestamptz")
    private Instant mfaAt;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;

    @Column(name = "login_at", columnDefinition = "timestamptz")
    private Instant loginAt;

    @Column(name = "session_expires_at", columnDefinition = "timestamptz")
    private Instant sessionExpiresAt;

    @Column(name = "logout_at", columnDefinition = "timestamptz")
    private Instant logoutAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    public String getAuthMethod() {
        return method;
    }

    public void setAuthMethod(String authMethod) {
        this.method = authMethod;
    }
}
