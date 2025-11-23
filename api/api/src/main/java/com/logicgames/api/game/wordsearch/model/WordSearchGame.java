package com.logicgames.api.game.wordsearch.model;

import com.logicgames.api.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "word_search_game")
public class WordSearchGame {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String state; // "IN_PROGRESS", "COMPLETED"

    @Column(nullable = false)
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    @Column(nullable = false)
    private int gridSize; // Ej: 10 para 10x10

    // La matriz completa convertida a String (ej. "A,X,H,J...")
    // Usaremos un String simple sin comas para ahorrar espacio: "AXHJ..."
    @Column(nullable = false, columnDefinition = "TEXT")
    private String matrixString;

    // Palabras objetivo (ej. "JAVA", "SPRING")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ws_target_words", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "word")
    private List<String> wordsToFind;

    // Palabras que el usuario ya encontró
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ws_found_words", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "word")
    private Set<String> foundWords;

    @Column(nullable = false)
    private Long timeLimitSeconds;

    @Column(nullable = false)
    private Long timeElapsedSeconds;

    @CreationTimestamp
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false)
    private String gameMode;

    @Column(nullable = false)
    private String theme;
}
