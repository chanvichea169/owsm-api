package com.owsm.AuthService.dto;

import lombok.Data;

@Data
public class AdministrativeLocationResponse {
    private String code;
    private String nameKh;
    private String nameEn;
    private String parentCode;
    private String status;
}
