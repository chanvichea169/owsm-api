package com.owsm.AuthService.dto;

import lombok.Data;

@Data
public class AdministrativeLocationResponse implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String nameKh;
    private String nameEn;
    private String parentCode;
    private String status;
}
