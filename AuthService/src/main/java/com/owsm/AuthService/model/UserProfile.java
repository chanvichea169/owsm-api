package com.owsm.AuthService.model;

import com.owsm.AuthService.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "tbl_user_profiles")
public class UserProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String firstNameEn;
    private String firstNameKh;
    private String lastNameEn;
    private String lastNameKh;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private String bioEn;
    private String bioKh;
    private String address;
    private String addressEn;
    private String addressKh;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private Date birthDate;
}