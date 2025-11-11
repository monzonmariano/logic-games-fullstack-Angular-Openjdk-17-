package com.logicgames.api.game.dtos;
import lombok.Data;

@Data
public class SaveGameRequest {

    // El tablero actual del usuario (con sus errores)
    private String boardString;

    // Los segundos que lleva jugados
    private Long timeElapsedSeconds;
}
