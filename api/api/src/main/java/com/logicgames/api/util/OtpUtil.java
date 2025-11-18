package com.logicgames.api.util;


import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class OtpUtil {

    /**
     * Genera un código OTP (One-Time Password) de 6 dígitos.
     * @return un String (ej. "123456")
     */

    // Instancia única y segura (mejor rendimiento que crear una nueva cada vez)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateOtp() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
