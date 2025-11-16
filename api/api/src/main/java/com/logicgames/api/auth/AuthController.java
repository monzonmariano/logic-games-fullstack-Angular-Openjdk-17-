package com.logicgames.api.auth;


import com.logicgames.api.auth.dtos.AuthenticationRequest;
import com.logicgames.api.auth.dtos.AuthenticationResponse;
import com.logicgames.api.auth.dtos.RegisterRequest;
import com.logicgames.api.auth.dtos.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController // <-- Post-it: Es un controlador de API
@RequestMapping("/api/auth") // Todas las rutas aquí empiezan con /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Escucha peticiones POST en /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request // @RequestBody convierte el JSON en el objeto
    ) {
        authService.register(request);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }
    // --- NUEVO MÉTODO DE LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        // Llama al nuevo método del servicio y devuelve la respuesta
        return ResponseEntity.ok(authService.login(request));
    }
    // --- "OLVIDÉ MI CONTRASEÑA"! ---
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody Map<String, String> request
    ) {
        // 1. Extraemos el email del cuerpo JSON
        String email = request.get("email");

        // 2. Llamamos a nuestro "cerebro"
        authService.requestPasswordReset(email);

        // 3. ¡Mensaje de Seguridad!
        // Siempre devolvemos "OK", incluso si el email no existe.
        return ResponseEntity.ok("Si el email está registrado, recibirás un enlace.");
    }

    // --- ¡NUEVA PUERTA PARA EJECUTAR EL RESETEO! ---
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Contraseña actualizada exitosamente.");
        } catch (IllegalStateException e) {
            // Captura los errores (ej. "Token caducado")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Devuelve un error 400 (¡no 403!)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        // Devuelve el mensaje de error (ej. "El email ya está en uso")
        // como el cuerpo de la respuesta.
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
