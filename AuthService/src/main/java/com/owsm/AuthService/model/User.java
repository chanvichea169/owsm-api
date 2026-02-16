package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "tblusers")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(unique = true)
    private String email;
    private String password;
    private String otp;
    private boolean enabled;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    private Date createdAt;
    private Date updatedAt;
}
