package com.logicgames.api.auth;

import com.logicgames.api.auth.dtos.AuthenticationRequest;
import com.logicgames.api.auth.dtos.AuthenticationResponse;
import com.logicgames.api.auth.dtos.RegisterRequest;
import com.logicgames.api.auth.dtos.ResetPasswordRequest;
import com.logicgames.api.jwt.JwtService; // <-- IMPORTA
import com.logicgames.api.user.User;
import com.logicgames.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager; // <-- IMPORTA
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // <-- IMPORTA
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import com.logicgames.api.email.EmailService;

@Service // <-- Post-it: "Esta es una clase de lógica de negocio"
@RequiredArgsConstructor // <-- Lombok: Crea un constructor por nosotros
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    // Lógica de Registro
    public void register(RegisterRequest request) {
        // 2. Comprobar si el usuario ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            // Sería mejor lanzar una excepción específica, pero esto funciona
            throw new IllegalStateException("El email ya está en uso");
        }

        // 3. Crear el objeto User (el "molde" de la BBDD)
        var user = User.builder()
                .email(request.getEmail())
                // 4. ¡Encriptar la contraseña ANTES de guardarla!
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 5. Guardar al usuario en la base de datos
        userRepository.save(user);


    }
    // --- NUEVO MÉTODO DE LOGIN ---
    public AuthenticationResponse login(AuthenticationRequest request) {
        // 1. Le pedimos al "Gerente" que autentique al usuario.
        // Esto comprueba automáticamente si el email existe y si la contraseña es correcta.
        // Si falla, lanzará una excepción (¡y no continuará!)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si llegamos aquí, el usuario es VÁLIDO.
        // Lo buscamos en la BBDD para obtener sus datos.
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Sabemos que existe, así que no necesitamos manejo de error

        // 3. Creamos el "carnet" (token) para este usuario.
        var jwtToken = jwtService.generateToken(user);

        // 4. Devolvemos la respuesta con el token.
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    // --- SOLICITAR RESETEO DE CONTRASEÑA! ---
    public void requestPasswordReset(String email) {

        // 1. Buscar al usuario por su email
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 2. ¡MUY IMPORTANTE! (Seguridad)
        // Si el email no existe, NO lanzamos un error.
        // Simplemente retornamos en silencio.
        // Esto evita que los hackers "pesquen" emails válidos.
        if (userOptional.isEmpty()) {
            System.out.println("Solicitud de reseteo para email (no encontrado): " + email);
            return; // No hacer nada y no dar pistas.
        }

        // 3. Generar el "pase" (token). Es un string aleatorio universal.
        String resetToken = UUID.randomUUID().toString();

        // 4. Establecer la caducidad (ej. 15 minutos desde ahora)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        // 5. Actualizar el usuario en la BBDD
        User user = userOptional.get();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiryDate);

        userRepository.save(user);

        // --- 6. SIMULACIÓN DE ENVÍO DE EMAIL ---
        // En un proyecto real, aquí llamaríamos a:
        // emailService.sendResetLink(user.getEmail(), resetToken);
        //

        emailService.sendResetLink(user.getEmail(), resetToken);
    }

    // --- MÉTODO PARA EJECUTAR EL RESETEO! ---
    public void resetPassword(ResetPasswordRequest request) {

        // 1. Busca al usuario usando el token
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalStateException("Token inválido o no encontrado"));

        // 2. Comprueba si el token ha caducado
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El token de reseteo ha caducado");
        }

        // 3. ¡Todo en orden! Hashea la nueva contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 4. ¡MUY IMPORTANTE! Invalida el token
        //    Poniéndolo a null, no se puede volver a usar.
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // 5. Guarda el usuario con su nueva contraseña
        userRepository.save(user);
    }
}
