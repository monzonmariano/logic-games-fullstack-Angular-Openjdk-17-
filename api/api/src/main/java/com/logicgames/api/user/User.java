package com.logicgames.api.user;

import jakarta.persistence.*; // Importante: usa jakarta.persistence
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    // El "pase de un solo uso"
    private String resetToken;

    // La fecha en que el "pase" caduca
    private LocalDateTime resetTokenExpiry;

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
