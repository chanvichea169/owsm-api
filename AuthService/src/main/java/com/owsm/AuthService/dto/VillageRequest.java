package com.owsm.AuthService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VillageRequest {
    @NotBlank
    @Size(max = 20)
    private String villageCode;

    @NotBlank
    private String villageKh;

    @NotBlank
    private String villageEn;

    @NotNull
    @Positive
    private Integer communeCode;
}
