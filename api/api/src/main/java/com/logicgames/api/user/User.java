package com.logicgames.api.user;

import jakarta.persistence.*; // Importante: usa jakarta.persistence
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Data // <-- Lombok: Nos da Getters, Setters, toString, etc.
@Builder // <-- Lombok: Para construir objetos de forma fácil
@NoArgsConstructor // <-- Lombok: Constructor vacío
@AllArgsConstructor // <-- Lombok: Constructor con todos los argumentos
@Entity // <-- JPA: Le dice a Spring que esto es una tabla de BBDD
@Table(name = "_user") // Nombramos la tabla "_user" (user solo puede dar problemas)
public class User implements UserDetails{

    @Id
    @GeneratedValue // El ID se auto-genera (1, 2, 3...)
    private Long id;

    @Column(unique = true, nullable = false) // No puede haber dos emails iguales
    private String email;

    @Column(nullable = false)
    private String password; // ¡Guardaremos la contraseña encriptada!

    // (Por defecto será 'false' cuando se cree)
    // --- PARA VERIFICACIÓN DE EMAIL ---
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isVerified;

    @Column(name = "verification_code") // Renombramos la columna
    private String verificationCode; // <-- Renombrado de 'otpCode'

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry; // <-- Renombrado de 'otpCodeExpiry'

    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    // --- ¡NUEVO! PARA RESETEO DE CONTRASEÑA ---
    private String resetCode; // <-- ¡El nuevo campo que faltaba!
    private LocalDateTime resetCodeExpiry;

    private String resetToken; // Para el enlace de reseteo
    private LocalDateTime resetTokenExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Para saber cuándo se creó el usuario
    // --- Métodos de UserDetails ---
    // UserDetails es el "carnet" que Spring Security sabe leer.
    // Le "traducimos" nuestro 'User' a lo que Spring Security entiende.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora, no tenemos roles (ADMIN, USER, etc.).
        // Devolvemos una lista vacía.
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }


    // Por ahora, asumimos que las cuentas están siempre activas.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
