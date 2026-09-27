package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_villages")
public class Village {

    @Id
    @Column(name = "village_code", nullable = false, length = 20)
    private String villageCode;

    @Column(name = "village_kh", nullable = false)
    private String villageKh;

    @Column(name = "village_en", nullable = false)
    private String villageEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_code", nullable = false)
    private Commune commune;
}