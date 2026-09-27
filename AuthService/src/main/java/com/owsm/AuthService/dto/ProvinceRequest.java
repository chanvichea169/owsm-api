package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProvinceRequest {
    @NotNull
    @Positive
    private Integer provinceCode;

    @NotBlank
    private String provinceKh;

    @NotBlank
    private String provinceEn;

    private String status;
}
