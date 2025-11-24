package com.logicgames.api.game.common.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder // Usaremos Builder para que sea fácil de crear
public class ScoreboardEntryDTO {
    private Long id;
    private String gameType; // <--- (Valores: "SUDOKU", "WORD_SEARCH")
    private String difficulty;
    private long timeElapsedSeconds;
    private LocalDateTime lastUpdatedAt;
    private String userEmail;
}
