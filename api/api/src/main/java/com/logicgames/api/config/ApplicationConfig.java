package com.logicgames.api.config;

import com.logicgames.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor // Lombok creará un constructor con UserRepository
public class ApplicationConfig {

    private final UserRepository userRepository;

    @Bean // <-- Un "Post-it" que dice: "Spring, crea esta herramienta"
    public PasswordEncoder passwordEncoder() {
        // Usamos BCrypt, el estándar de oro para encriptar contraseñas
        return new BCryptPasswordEncoder();
    }

    // 1. El "Buscador de Usuarios" (UserDetailsService)
    // Le dice a Spring Security CÓMO buscar a un usuario por su email.
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // 2. El "Proveedor de Autenticación" (AuthenticationProvider)
    // Es el "gerente" que une el "Buscador de Usuarios" (paso 1)
    // con el "Encriptador de Contraseñas" (el bean que ya tenías).
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. El "Gerente General de Autenticación" (AuthenticationManager)
    // El "cerebro" que usará nuestro AuthService para procesar un login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
