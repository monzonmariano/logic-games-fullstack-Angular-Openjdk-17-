package com.logicgames.api.game;

import com.logicgames.api.game.dtos.ScoreboardEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.logicgames.api.game.dtos.SaveGameRequest;
import com.logicgames.api.game.dtos.SudokuSolutionRequest;
import java.security.Principal;// <-- ¡La herramienta para saber "quién" llama!
import java.util.List;

@RestController
@RequestMapping("/api/sudoku") // Post-it: ¡Todas las URLs aquí empiezan con /api/sudoku!
@RequiredArgsConstructor
public class SudokuController {
    private final SudokuService sudokuService;

    /**
     * Endpoint para cargar o crear una partida.
     */
    @GetMapping("/load-or-create")
    public ResponseEntity<SudokuGame> loadOrCreateGame(
            Principal principal,
            @RequestParam String difficulty,
            @RequestParam String gameMode
    ) {
        String userEmail = principal.getName();
        SudokuGame game = sudokuService.loadOrCreateGame(userEmail, difficulty,gameMode);
        return ResponseEntity.ok(game);
    }

    /**
     *
     * Endpoint para guardar el progreso (Pausa).
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveGame(
            Principal principal,
            @RequestBody SaveGameRequest request // <-- Usa el DTO de "Guardar"
    ) {
        String userEmail = principal.getName();
        sudokuService.saveGameProgress(userEmail, request);

        // Devuelve 200 OK (vacío)
        return ResponseEntity.ok().build();
    }

    /**
     *
     * Endpoint para comprobar la solución final.
     */
    @PostMapping("/complete")
    public ResponseEntity<Boolean> completeGame(
            Principal principal,
            @RequestBody SudokuSolutionRequest request // <-- Usa el DTO de "Solución"
    ) {
        String userEmail = principal.getName();

        // Llama al "cerebro" y devuelve true (ganó) o false (falló)
        boolean didWin = sudokuService.completeGame(userEmail, request);

        return ResponseEntity.ok(didWin);
    }
    /**
     *
     * Endpoint para marcar la partida como fallida (tiempo agotado).
     */
    @PostMapping("/fail")
    public ResponseEntity<Void> failGame(Principal principal) {
        String userEmail = principal.getName();
        sudokuService.failGame(userEmail);
        return ResponseEntity.ok().build();
    }
    /**
     *
     * Endpoint para obtener el historial de partidas completadas (scoreboard).
     */
    @GetMapping("/scoreboard")
    public ResponseEntity<List<ScoreboardEntryDTO>> getScoreboard(Principal principal) {
        String userEmail = principal.getName();

        // Llama al "cerebro"
        List<ScoreboardEntryDTO> scoreboard = sudokuService.getScoreboard(userEmail);
        return ResponseEntity.ok(scoreboard);
    }
}
