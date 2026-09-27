package com.owsm.AuthService.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_provinces")
public class Province {

    @Id
    @Column(name = "province_code", nullable = false)
    private Integer provinceCode;

    @Column(name = "province_kh", nullable = false)
    private String provinceKh;

    @Column(name = "province_en", nullable = false)
    private String provinceEn;

    private String status;
}