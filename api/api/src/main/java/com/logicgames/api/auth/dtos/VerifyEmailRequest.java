package com.logicgames.api.auth.dtos;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String email;
    private String otpCode;
}
