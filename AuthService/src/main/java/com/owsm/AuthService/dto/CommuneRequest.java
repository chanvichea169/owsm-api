package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommuneRequest {
    @NotNull
    @Positive
    private Integer communeCode;

    @NotBlank
    private String communeKh;

    @NotBlank
    private String communeEn;

    @NotNull
    @Positive
    private Integer districtCode;
}
