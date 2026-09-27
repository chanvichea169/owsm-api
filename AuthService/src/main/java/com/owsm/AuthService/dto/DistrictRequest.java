package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DistrictRequest {
    @NotNull
    @Positive
    private Integer districtCode;

    @NotBlank
    private String districtKh;

    @NotBlank
    private String districtEn;

    @NotNull
    @Positive
    private Integer provinceCode;
}
