package com.logicgames.api.game.dtos;

import lombok.Data;

@Data
public class SudokuSolutionRequest {
    // El string de 81 caracteres que el usuario
    // cree que es la solución (ej. "5346...")
    private String boardString;
}
