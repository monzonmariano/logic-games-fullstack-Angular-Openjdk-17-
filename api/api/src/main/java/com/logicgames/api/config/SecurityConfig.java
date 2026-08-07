package com.logicgames.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.logicgames.api.security.service.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity // Activa la seguridad web de Spring
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //HOla
                // --- ¡¡LA CONFIGURACIÓN DE CORS DEFINITIVA!! ---
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            "https://logic-games.netlify.app", // Producción (Netlify)
                            "http://localhost:4200",         // Dev (Angular local)
                            "http://localhost:8081",          // Dev (Docker local)
                            "https://pseudoanatomic-joie-ferally.ngrok-free.dev" // Pruebas locales desde el movil
                    ));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))


                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- RUTAS DE AUTENTICACIÓN ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/resend-verification").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/verify-email-link").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/reset-password-link").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password-code").permitAll()

                        // --- EL DESPERTADOR DE NEONDB ---
                        .requestMatchers(HttpMethod.GET, "/api/Hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ping-db").permitAll() // <-- ¡AQUÍ ESTÁ!

                        // --- RUTAS PÚBLICAS DE JUEGOS (MODO INVITADO / ZEN) ---
                        // Nota: Aquí debes poner la ruta exacta que usa tu frontend
                        // para pedir un tablero nuevo sin estar logueado.
                        // .requestMatchers(HttpMethod.GET, "/api/sudoku/generate").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/wordsearch/generate").permitAll()

                        // Todo lo demás, bloqueado (ej. guardar tiempos y puntajes)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ¡Añade tu filtro "lector de carnets" ANTES del filtro normal de login!
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
