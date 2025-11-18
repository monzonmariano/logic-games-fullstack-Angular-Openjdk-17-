package com.logicgames.api.game.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SudokuSolutionRequest {
    // El string de 81 caracteres que el usuario
    // cree que es la solución (ej. "5346...")

    @NotBlank(message = "La solución es obligatoria")
    @Size(min = 81, max = 81, message = "La solución debe tener exactamente 81 caracteres")
    private String boardString;

    @NotNull(message = "El tiempo es obligatorio")
    @Min(value = 0, message = "El tiempo no puede ser negativo")
    private long timeElapsedSeconds;
}
