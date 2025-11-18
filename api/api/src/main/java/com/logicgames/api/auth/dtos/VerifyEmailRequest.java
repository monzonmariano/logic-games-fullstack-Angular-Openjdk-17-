package com.logicgames.api.auth.dtos;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


@Data
public class VerifyEmailRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe ser de 6 dígitos numéricos")
    private String otpCode;
}
