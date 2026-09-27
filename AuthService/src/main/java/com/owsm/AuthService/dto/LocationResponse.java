package com.owsm.AuthService.dto;

import lombok.Data;

@Data
public class LocationResponse {
    private String villageCode;
    private String villageEn;
    private String villageKh;

    private Integer communeCode;
    private String communeEn;
    private String communeKh;

    private Integer districtCode;
    private String districtEn;
    private String districtKh;

    private Integer provinceCode;
    private String provinceEn;
    private String provinceKh;
}