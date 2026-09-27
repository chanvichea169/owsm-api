package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_districts")
public class District {

    @Id
    @Column(name = "district_code", nullable = false)
    private Integer districtCode;

    @Column(name = "district_kh", nullable = false)
    private String districtKh;

    @Column(name = "district_en", nullable = false)
    private String districtEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", nullable = false)
    private Province province;
}