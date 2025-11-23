package com.logicgames.api.game.wordsearch.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pre_generated_word_search")
public class PreGeneratedWordSearch {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private int gridSize;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String matrixString;

    // Guardamos las palabras como un solo String separado por comas para simplificar el Pool
    // Ej: "JAVA,SPRING,DOCKER"
    @Column(nullable = false, columnDefinition = "TEXT")
    private String wordsToFindString;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


