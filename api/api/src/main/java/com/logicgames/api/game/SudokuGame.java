package com.logicgames.api.game;

import java.time.LocalDateTime;
import com.logicgames.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Post-it: Escribe Getters, Setters, toString...
@Builder // Post-it: Escribe el constructor "fluido"
@NoArgsConstructor // Post-it: Escribe un constructor vacío
@AllArgsConstructor // Post-it: Escribe un constructor con todos los campos
@Entity // Post-it: ¡Esto es una tabla de BBDD!
@Table(name = "sudoku_game") // Le damos un nombre limpio a la tabla
public class SudokuGame {

    @Id // Post-it: Esta es la Clave Primaria
    @GeneratedValue // Post-it: La BBDD numera esto automáticamente
    private Long id;

    // --- ¡LA CONEXIÓN CLAVE! ---
    @ManyToOne // Post-it: "Muchas de ESTAS partidas (SudokuGame) pertenecen a UN Usuario"
    @JoinColumn(name = "user_id", nullable = false) // Define la columna "foreign key"
    private User user; // Guarda el objeto 'User' completo

    // --- ESTADO DEL JUEGO ---

    @Column(length = 81, nullable = false) // 81 caracteres, no puede ser nulo
    private String boardString; // El tablero actual del jugador (ej. "5300...")

    @Column(length = 81, nullable = false)
    private String solutionString; // La solución (ej. "5346...")

    @Column(nullable = false)
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    @Column(nullable = false)
    private String state; // "IN_PROGRESS", "COMPLETED", "FAILED"

    // --- LÓGICA DEL TEMPORIZADOR ---

    @Column(nullable = false)
    private String gameMode; // "TIMED" (Por Tiempo) o "FREE" (Libre)

    @Column(nullable = false)
    private Long timeLimitSeconds; // Límite de tiempo (0 si es "FREE")

    @Column(nullable = false)
    private Long timeElapsedSeconds; // Segundos jugados (el cronómetro)

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt; // La última vez que se tocó esta fila
}
