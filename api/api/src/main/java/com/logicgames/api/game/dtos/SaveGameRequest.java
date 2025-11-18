package com.logicgames.api.game.dtos;
import lombok.Data;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class SaveGameRequest {

    // El tablero actual del usuario (con sus errores)
    @NotBlank(message = "El tablero no puede estar vacío")
    @Size(min = 81, max = 81, message = "El tablero debe tener exactamente 81 caracteres")
    // Opcional: Validar que solo contenga números
    // @Pattern(regexp = "^[0-9]+$", message = "El tablero solo debe contener números")
    private String boardString;

    // Los segundos que lleva jugados
    @NotNull(message = "El tiempo es obligatorio")
    @Min(value = 0, message = "El tiempo no puede ser negativo")
    private Long timeElapsedSeconds;
}
