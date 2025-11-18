package com.logicgames.api.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordWithCodeRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe ser de 6 dígitos numéricos")
    private String otpCode;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "Minimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@!%*?&]).+$",
            message = "Debe ser segura (Mayúscula, minúscula, número, símbolo)")
    private String newPassword;
}
