package com.owsm.AuthService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class VerifyOtpRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("otp")
    private String otp;
}
