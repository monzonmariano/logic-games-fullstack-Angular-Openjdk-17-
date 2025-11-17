package com.logicgames.api.util;


import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class OtpUtil {

    /**
     * Genera un código OTP (One-Time Password) de 6 dígitos.
     * @return un String (ej. "123456")
     */
    public String generateOtp() {
        // Genera un número entre 100000 y 999999
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}
