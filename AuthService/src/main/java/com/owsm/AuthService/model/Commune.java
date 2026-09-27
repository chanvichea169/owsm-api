package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_communes")
public class Commune {

    @Id
    @Column(name = "commune_code", nullable = false)
    private Integer communeCode;

    @Column(name = "commune_kh", nullable = false)
    private String communeKh;

    @Column(name = "commune_en", nullable = false)
    private String communeEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code", nullable = false)
    private District district;
}