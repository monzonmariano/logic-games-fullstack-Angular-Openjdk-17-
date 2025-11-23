package com.logicgames.api.game.sudoku.service;

import com.logicgames.api.game.common.repository.GameMetricRepository;
import com.logicgames.api.game.sudoku.repository.PreGeneratedPuzzleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class) // 1. Habilita Mockito para simular dependencias
public class SudokuGeneratorServiceTest {
    // 2. Simulamos (Mockeamos) los repositorios porque NO queremos ir a la BBDD real
    @Mock
    private PreGeneratedPuzzleRepository puzzleRepository;
    @Mock
    private GameMetricRepository metricRepository;

    // 3. Inyectamos los mocks dentro de nuestro servicio real
    @InjectMocks
    private SudokuGeneratorService generatorService;

    @Test
    @DisplayName("Debe generar un tablero con estructura válida (81 chars)")
    void shouldGenerateValidStructure() {
        // GIVEN (Dado)
        String difficulty = "MEDIUM";

        // WHEN (Cuando ejecutamos)
        var puzzle = generatorService.generateSudokuSync(difficulty);

        // THEN (Entonces verificamos)
        assertThat(puzzle).isNotNull();

        // El tablero debe tener 81 caracteres (9x9)
        assertThat(puzzle.getBoardString()).hasSize(81);
        // La solución también
        assertThat(puzzle.getSolutionString()).hasSize(81);

        // La solución NO debe tener ceros (debe estar resuelta)
        assertThat(puzzle.getSolutionString()).doesNotContain("0");

        // El tablero jugable SÍ debe tener ceros (huecos)
        assertThat(puzzle.getBoardString()).contains("0");
    }

    @Test
    @DisplayName("La dificultad HARD debe tener más huecos que EASY")
    void shouldRespectDifficulty() {
        // WHEN
        var easyPuzzle = generatorService.generateSudokuSync("EASY");
        var hardPuzzle = generatorService.generateSudokuSync("HARD");

        // Contamos cuántos ceros (huecos) tiene cada uno
        long easyZeros = easyPuzzle.getBoardString().chars().filter(ch -> ch == '0').count();
        long hardZeros = hardPuzzle.getBoardString().chars().filter(ch -> ch == '0').count();

        System.out.println("Huecos Easy: " + easyZeros + " vs Hard: " + hardZeros);

        // THEN
        assertThat(hardZeros).isGreaterThan(easyZeros);
    }

    @Test
    @DisplayName("La solución generada debe ser un Sudoku matemáticamente válido")
    void solutionShouldBeMathematicallyValid() {
        // WHEN
        var puzzle = generatorService.generateSudokuSync("MEDIUM");
        String solution = puzzle.getSolutionString();

        // Convertimos el string a matriz int[][]
        int[][] grid = parseGrid(solution);

        // THEN: Validamos filas y columnas
        assertThat(isValidSudoku(grid)).isTrue();
    }

    // --- MÉTODOS PRIVADOS DE AYUDA PARA EL TEST (VALIDADORES) ---

    private int[][] parseGrid(String s) {
        int[][] grid = new int[9][9];
        for (int i = 0; i < 81; i++) {
            grid[i / 9][i % 9] = Character.getNumericValue(s.charAt(i));
        }
        return grid;
    }

    private boolean isValidSudoku(int[][] board) {
        for (int i = 0; i < 9; i++) {
            // Verifica Fila
            if (!isValidUnit(board[i])) return false;

            // Verifica Columna
            int[] col = new int[9];
            for (int j = 0; j < 9; j++) col[j] = board[j][i];
            if (!isValidUnit(col)) return false;
        }
        return true;
    }

    private boolean isValidUnit(int[] unit) {
        boolean[] seen = new boolean[10]; // Indices 1-9
        for (int num : unit) {
            if (num < 1 || num > 9 || seen[num]) return false;
            seen[num] = true;
        }
        return true;
    }
}
