package com.logicgames.api.auth;

import com.logicgames.api.auth.dtos.AuthenticationRequest;
import com.logicgames.api.auth.dtos.AuthenticationResponse;
import com.logicgames.api.auth.dtos.RegisterRequest;
import com.logicgames.api.auth.dtos.ResetPasswordRequest;
import com.logicgames.api.jwt.JwtService;
import com.logicgames.api.user.User;
import com.logicgames.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.logicgames.api.util.OtpUtil;


import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import com.logicgames.api.email.EmailService;

@Service // <-- Post-it: "Esta es una clase de lógica de negocio"
@RequiredArgsConstructor // <-- Lombok: Crenosotrosa un constructor por
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    private final OtpUtil otpUtil;
    // Lógica de Registro
    // en AuthService.java
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está en uso");
        }

        String verificationCode = otpUtil.generateOtp(); // Código de 6 dígitos
        String verificationLinkToken = UUID.randomUUID().toString(); // Token de enlace

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(false)
                .otpCode(verificationCode) // Guarda el código
                .otpCodeExpiry(LocalDateTime.now().plusMinutes(15))
                .verificationToken(verificationLinkToken) // ¡Guarda el token!
                .verificationTokenExpiry(LocalDateTime.now().plusDays(1)) // Los enlaces duran más
                .build();

        userRepository.save(user);

        // ¡Envía AMBAS cosas al EmailService!
        emailService.sendVerificationEmail(request.getEmail(), verificationCode, verificationLinkToken);
    }
    // --- NUEVO MÉTODO DE LOGIN ---
    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // --- ¡LA NUEVA COMPROBACIÓN! ---
        if (!user.isVerified()) {
            // Si no está verificado, ¡lanza un error!
            // Esto será atrapado por el @ExceptionHandler en AuthController
            throw new IllegalStateException("Por favor, verifica tu email antes de iniciar sesión.");
        }
        // -------------------------------

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    // --- SOLICITAR RESETEO DE CONTRASEÑA! ---
    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return; // ¡Se queda mudo!
        }

        String resetCode = otpUtil.generateOtp();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        User user = userOptional.get();
        user.setOtpCode(resetCode); // <-- Campo renombrado
        user.setOtpCodeExpiry(expiryDate); // <-- Campo renombrado

        userRepository.save(user);

        // ¡Ahora envía el CÓDIGO, no el enlace!
        emailService.sendPasswordResetCode(user.getEmail(), resetCode);
    }

    // --- MÉTODO PARA EJECUTAR EL RESETEO! ---
    public void resetPassword(ResetPasswordRequest request) {

        // 1. Busca al usuario por el CÓDIGO (¡no por el token!)
        User user = userRepository.findByOtpCode(request.getToken()) // ¡Necesitaremos crear este método!
                .orElseThrow(() -> new IllegalStateException("Código inválido o no encontrado"));

        // 2. Comprueba si ha caducado
        if (user.getOtpCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código de reseteo ha caducado");
        }

        // 3. Hashea la nueva contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 4. Invalida el código
        user.setOtpCode(null);
        user.setOtpCodeExpiry(null);

        userRepository.save(user);
    }

    public void verifyEmail(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // 1. Comprueba si ya está verificado
        if (user.isVerified()) {
            throw new IllegalStateException("Este email ya ha sido verificado.");
        }

        // 2. Comprueba si el código es correcto
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otpCode)) {
            throw new IllegalStateException("El código de verificación es incorrecto.");
        }

        // 3. Comprueba si ha caducado
        if (user.getOtpCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código de verificación ha caducado.");
        }

        // 4. ¡Todo bien! Verifica al usuario
        user.setVerified(true);
        user.setOtpCode(null); // Invalida el código
        user.setOtpCodeExpiry(null);

        userRepository.save(user);
    }

    // --- MÉTODO PARA EL ENLACE! ---
    public void verifyEmailLink(String token) {
        // Busca al usuario por el token del enlace
        User user = userRepository.findByVerificationToken(token) // <-- ¡Tendremos que crear esto!
                .orElseThrow(() -> new IllegalStateException("Enlace de verificación inválido o no encontrado."));

        // Comprueba si ha caducado
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace de verificación ha caducado. Por favor, solicita uno nuevo.");
        }

        // ¡Todo bien! Verifica al usuario
        user.setVerified(true);
        user.setOtpCode(null); // Invalida ambos
        user.setOtpCodeExpiry(null);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        userRepository.save(user);
        // ¡En este punto, el frontend debería redirigir al login!
    }
    // (Llamado por AuthController) ---
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (user.isVerified()) {
            throw new IllegalStateException("Este email ya ha sido verificado.");
        }

        // --- ¡LA NUEVA LÓGICA! ---
        // 1. Genera AMBOS, un nuevo código Y un nuevo enlace
        String newVerificationCode = otpUtil.generateOtp();
        String newVerificationLinkToken = UUID.randomUUID().toString();

        // 2. Actualiza el usuario con AMBOS valores nuevos
        user.setOtpCode(newVerificationCode);
        user.setOtpCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setVerificationToken(newVerificationLinkToken); // <-- ¡Añade esto!
        user.setVerificationTokenExpiry(LocalDateTime.now().plusDays(1)); // <-- ¡Añade esto!

        userRepository.save(user);

        // 3. ¡Llama al método EmailService correcto con 3 argumentos!
        emailService.sendVerificationEmail(email, newVerificationCode, newVerificationLinkToken);
    }
}
