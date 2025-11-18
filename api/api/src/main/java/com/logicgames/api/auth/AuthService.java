package com.logicgames.api.auth;

import com.logicgames.api.auth.dtos.*;
import com.logicgames.api.email.EmailService;
import com.logicgames.api.jwt.JwtService;
import com.logicgames.api.user.User;
import com.logicgames.api.user.UserRepository;
import com.logicgames.api.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpUtil otpUtil;

    // ========================================================================
    // SECCIÓN 1: REGISTRO Y VERIFICACIÓN DE EMAIL
    // ========================================================================

    /**
     * 1. Registro inicial: Crea el usuario (no verificado), genera códigos y envía el email.
     */
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está en uso");
        }

        // Generamos AMBOS: Código (para móvil) y Token (para enlace PC)
        String verificationCode = otpUtil.generateOtp();
        String verificationLinkToken = UUID.randomUUID().toString();

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(false) // Nace bloqueado
                // Datos del código de 6 dígitos
                .verificationCode(verificationCode)
                .verificationCodeExpiry(LocalDateTime.now().plusMinutes(15))
                // Datos del enlace
                .verificationToken(verificationLinkToken)
                .verificationTokenExpiry(LocalDateTime.now().plusDays(1))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(request.getEmail(), verificationCode, verificationLinkToken);
    }

    /**
     * 2. Verificar usando el CÓDIGO de 6 dígitos (Desde /verify-email).
     */
    public void verifyEmail(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (user.isVerified()) throw new IllegalStateException("Este email ya ha sido verificado.");

        // Validaciones del código
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(otpCode)) {
            throw new IllegalStateException("El código de verificación es incorrecto.");
        }
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código de verificación ha caducado.");
        }

        verifyUser(user); // ¡Validado!
    }

    /**
     * 3. Verificar usando el ENLACE (Desde el email).
     */
    public void verifyEmailLink(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("Enlace inválido o no encontrado."));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace ha caducado.");
        }

        verifyUser(user); // ¡Validado!
    }

    /**
     * 4. Reenviar verificación: Genera nuevos códigos y reenvía el email.
     */
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (user.isVerified()) throw new IllegalStateException("Este email ya ha sido verificado.");

        // Regeneramos todo
        String newCode = otpUtil.generateOtp();
        String newToken = UUID.randomUUID().toString();

        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        emailService.sendVerificationEmail(email, newCode, newToken);
    }

    // ========================================================================
    // SECCIÓN 2: INICIO DE SESIÓN (LOGIN)
    // ========================================================================

    public AuthenticationResponse login(AuthenticationRequest request) {
        // 1. Spring Security comprueba email y contraseña
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Buscamos al usuario para comprobar si está verificado
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!user.isVerified()) {
            throw new IllegalStateException("Por favor, verifica tu email antes de iniciar sesión.");
        }

        // 3. Generamos el token JWT (la sesión)
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    // ========================================================================
    // SECCIÓN 3: RECUPERACIÓN DE CONTRASEÑA
    // ========================================================================

    /**
     * A. El usuario pide recuperar contraseña ("Olvidé contraseña").
     * Generamos códigos y enviamos email.
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Generamos AMBOS
        String resetCode = otpUtil.generateOtp();
        String resetToken = UUID.randomUUID().toString();

        // Guardamos en los campos de RESETEO (no en los de verificación)
        user.setResetCode(resetCode);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetCode, resetToken);
    }

    /**
     * B. Resetear usando el ENLACE (Token).
     */
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalStateException("Enlace inválido o no encontrado"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace ha caducado.");
        }

        completePasswordReset(user, request.getNewPassword());
    }

    /**
     * C. Resetear usando el CÓDIGO de 6 dígitos.
     * (Este era el método que faltaba).
     */
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Validamos el código de reseteo
        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            throw new IllegalStateException("Código de reseteo incorrecto.");
        }
        if (user.getResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código ha caducado.");
        }

        completePasswordReset(user, newPassword);
    }

    // ========================================================================
    // MÉTODOS PRIVADOS "HELPER" (Para no repetir código)
    // ========================================================================

    // Limpia los códigos de verificación y marca como verificado
    private void verifyUser(User user) {
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    // Cambia la contraseña y limpia los códigos de reseteo
    private void completePasswordReset(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword)); // ¡Encripta!
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}