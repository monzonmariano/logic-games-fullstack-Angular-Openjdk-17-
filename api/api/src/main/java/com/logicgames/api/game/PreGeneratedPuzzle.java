package com.logicgames.api.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pre_generated_puzzles")
public class PreGeneratedPuzzle {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    @Column(nullable = false, length = 100)
    private String boardString;

    @Column(nullable = false, length = 100)
    private String solutionString;

    // --- ¡AÑADE ESTE CAMPO! ---
    @CreationTimestamp // ¡Hibernate rellena esto automáticamente al crear!
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


}
