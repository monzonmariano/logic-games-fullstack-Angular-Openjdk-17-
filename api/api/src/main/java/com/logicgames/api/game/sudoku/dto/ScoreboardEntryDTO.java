package com.logicgames.api.game.sudoku.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder // Usaremos Builder para que sea fácil de crear
public class ScoreboardEntryDTO {
    private Long id;
    private String difficulty;
    private long timeElapsedSeconds;
    private LocalDateTime lastUpdatedAt;
    private String userEmail;
}
