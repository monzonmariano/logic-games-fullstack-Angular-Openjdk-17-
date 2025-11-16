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
@Table(name = "game_metrics")
public class GameMetric {

    @Id
    @GeneratedValue
    private Long id;

    // El tipo de evento que estamos registrando
    // Ej. "PUZZLE_CONSUMED_EASY", "GAME_COMPLETED_HARD", "USER_REGISTERED"
    @Column(nullable = false)
    private String eventType;

    // La hora en que ocurrió
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;
}
