package com.owsm.AuthService.model;

import com.owsm.AuthService.common.audit.BaseEntity;
import com.owsm.AuthService.enumeration.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_roles")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleName name;

    @Column(name = "name_en", length = 64)
    private String nameEn;

    @Column(name = "name_kh", length = 128)
    private String nameKh;

    @Column(length = 255, nullable = true)
    private String description;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_kh")
    private String descriptionKh;
}
