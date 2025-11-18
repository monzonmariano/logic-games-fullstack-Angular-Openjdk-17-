package com.logicgames.api.auth.dtos;


import lombok.Data;

@Data
public class ResetPasswordWithCodeRequest {

    private String email;
    private String otpCode;
    private String newPassword;
}
