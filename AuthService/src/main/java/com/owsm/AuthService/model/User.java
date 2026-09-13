package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    /** Email OTP verified */
    @Column(nullable = false)
    private boolean enabled = false;

    /** Admin-controlled enable/disable */
    @Column(nullable = false)
    private boolean active = true;

    private String otp;
    @Column(name = "otp_created_at")
    private LocalDateTime otpCreatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}