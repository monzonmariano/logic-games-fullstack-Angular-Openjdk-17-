package com.logicgames.api.jwt;


import com.logicgames.api.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends  OncePerRequestFilter {

    private  final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Coge la cabecera "Authorization" de la petición
        final String authHeader = request.getHeader("Authorization");


        // 2. Si no hay cabecera, o no empieza con "Bearer ",
        //    no es nuestra petición. La dejamos pasar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrae el "carnet" (token) (quitando "Bearer ")
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            // 4. Usa el JwtService para "leer" el carnet y sacar el email
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Si el token está caducado o es falso, fallará.
            System.err.println("Token JWT inválido: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Si tenemos email Y el usuario NO está ya autenticado...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Carga los detalles del usuario desde la BBDD
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);




            // 7. ¡AHORA SÍ! Validamos el token
            //Si el token NO es válido,  el 'if' se salta y el usuario no se autentica.

            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. Creamos un "ticket de autenticación" para Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. ¡Ponemos el "ticket" en el "contexto de seguridad"!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }

        // 10. Dejamos pasar la petición (ahora autenticada)
        filterChain.doFilter(request, response);
    }
}
