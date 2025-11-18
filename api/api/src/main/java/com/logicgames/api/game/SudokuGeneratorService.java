package com.logicgames.api.game;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SudokuGeneratorService {

    private final PreGeneratedPuzzleRepository puzzleRepository;
    private final GameMetricRepository metricRepository;

    private static final Random RANDOM = new Random();

    private static final int BOARD_SIZE = 9;
    private static final int SUB_GRID_SIZE = 3;

    public static class GeneratedPuzzle {
        String boardString;
        String solutionString;
        GeneratedPuzzle(String b, String s) { boardString = b; solutionString = s; }
    }

    /**
     * ¡Tu método 'generateSudoku', renombrado a 'Sync' para claridad!
     */
    public GeneratedPuzzle generateSudokuSync(String difficulty) {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

        populateBoard(board);
        String solutionString = gridToString(board);

        int numRemoves = 43;
        if ("EASY".equals(difficulty)) numRemoves = 33;
        if ("HARD".equals(difficulty)) numRemoves = 53;

        removeCells(board, numRemoves);
        String boardString = gridToString(board);

        return new GeneratedPuzzle(boardString, solutionString);
    }

    /**
     * Tu método 'populateBoard' (sin cambios)
     */
    private static void populateBoard(int[][] board) {
        if (!solve(board, 0, 0)) {
            throw new IllegalStateException("No se pudo generar un sudoku válido.");
        }
    }

    /**
     * Tu método 'solve' (sin cambios)
     */
    private static boolean solve(int[][] board, int row, int col) {
        if (col == BOARD_SIZE) {
            col = 0;
            row++;
            if (row == BOARD_SIZE) {
                return true;
            }
        }

        if (board[row][col] != 0) {
            return solve(board, row, col + 1);
        }


        for (int num : RANDOM.ints(1, BOARD_SIZE + 1).distinct().limit(BOARD_SIZE).toArray()) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (solve(board, row, col + 1)) {
                    return true;
                }
            }
        }

        board[row][col] = 0;
        return false;
    }

    /**
     * Tu método 'isValid' (sin cambios)
     */
    private static boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) {
                return false;
            }
        }

        int r = row - row % SUB_GRID_SIZE;
        int c = col - col % SUB_GRID_SIZE;
        for (int i = r; i < r + SUB_GRID_SIZE; i++) {
            for (int j = c; j < c + SUB_GRID_SIZE; j++) {
                if (board[i][j] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Tu método 'removeCells'
     */
    private static void removeCells(int[][] board, int numRemoves) {

        for (int i = 0; i < numRemoves; i++) {
            int row = RANDOM.nextInt(BOARD_SIZE);
            int col = RANDOM.nextInt(BOARD_SIZE);
            if (board[row][col] != 0) {
                board[row][col] = 0;
            } else {
                i--;
            }
        }
    }

    /**
     * Tu "helper" para convertir int[][] a String
     */
    private String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder(81);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sb.append(grid[i][j]);
            }
        }
        return sb.toString();
    }
    // --- FIN DE TU ALGORITMO ---


    /**
     * ¡El método asíncrono!
     * Se ejecuta en un hilo separado para rellenar el pool.
     */
    @Async
    public void populatePoolAsync(String difficulty) {
        System.out.println("-> TAREA ASÍNCRONA: Iniciando generación de pool para " + difficulty);
        // 1. Define el tipo de métrica a buscar
        String eventType = "PUZZLE_CONSUMED_" + difficulty.toUpperCase(); // ej. "PUZZLE_CONSUMED_MEDIUM"

        // 2. Define la ventana de tiempo (ej. la última hora)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // 3. ¡Consulta la BBDD!
        long demandLastHour = metricRepository.countByEventTypeAndEventTimestampAfter(eventType, oneHourAgo);
        // 4. ¡Toma la decisión!
        int puzzlesToGenerate = 10; // Generación estándar
        if (demandLastHour > 5) { // Si se usaron más de 5 puzzles en la última hora...
            puzzlesToGenerate = 20; // ...¡duplica la generación!
            System.out.println("-> TAREA ASÍNCRONA: ¡Demanda alta detectada! (" + demandLastHour + ") Generando 20 puzzles.");
        } else {
            System.out.println("-> TAREA ASÍNCRONA: (Demanda baja) Iniciando generación de pool de 10 para " + difficulty);
        }
        try {
            // 5. ¡Usa la nueva variable en el bucle!
            for (int i = 0; i < puzzlesToGenerate; i++) {
                GeneratedPuzzle puzzle = this.generateSudokuSync(difficulty);

                PreGeneratedPuzzle prePuzzle = PreGeneratedPuzzle.builder()
                        .difficulty(difficulty)
                        .boardString(puzzle.boardString)
                        .solutionString(puzzle.solutionString)
                        .build(); // (El 'createdAt' se añade solo)

                puzzleRepository.save(prePuzzle);
            }
            System.out.println("-> TAREA ASÍNCRONA: Pool rellenado para " + difficulty);
        } catch (Exception e) {
            System.err.println("-> TAREA ASÍNCRONA: Falló la generación del pool: " + e.getMessage());
        }
    }
}
