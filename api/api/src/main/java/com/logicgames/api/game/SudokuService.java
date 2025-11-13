package com.logicgames.api.game;




import com.logicgames.api.user.User;
import com.logicgames.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.logicgames.api.game.dtos.SudokuSolutionRequest;
import com.logicgames.api.game.dtos.SaveGameRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.List;

@Service // Post-it: "Soy un Cerebro (Lógica de Negocio)"
@RequiredArgsConstructor // Post-it: ¡Crea mi constructor para inyectar mis herramientas!
public class SudokuService {


    // El Archivador de Sudoku (para buscar/guardar partidas)
    private final SudokuGameRepository sudokuGameRepository;

    // El Archivador de User (para encontrar al jugador)
    private final UserRepository userRepository;

    // --- 1. Las Herramientas que necesitamos ---




    /**
     * Lógica principal: Carga la partida "IN_PROGRESS" de un usuario.
     * Si no existe, o si la dificultad es diferente, crea una NUEVA.
     * Esta es la lógica de "sobrescribir" que discutimos.
     */
    public SudokuGame loadOrCreateGame(String userEmail, String difficulty, String gameMode) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Optional<SudokuGame> existingGameOpt = sudokuGameRepository
                .findByUserAndState(user, "IN_PROGRESS");

        if (existingGameOpt.isPresent()) {
            SudokuGame game = existingGameOpt.get();

            //
            // ¡Comprueba AMBAS cosas!
            if (game.getDifficulty().equals(difficulty) && game.getGameMode().equals(gameMode)) {
                // Si la dificultad Y el modo coinciden, ¡retoma la partida!
                System.out.println("Partida en progreso encontrada para: " + userEmail);
                return game;
            }

            // Si la dificultad O el modo son DIFERENTES, sobrescribimos.
            System.out.println("Sobrescribiendo partida en progreso para: " + userEmail);
            // ¡Usamos 'game' (el objeto viejo) para actualizarlo y no crear uno nuevo!
            SudokuGame updatedGame = generateNewBoard(user, difficulty, gameMode, game);
            return sudokuGameRepository.save(updatedGame); // ¡Guarda la actualización!

        } else {
            // No había ninguna. Creamos una nueva.
            System.out.println("Creando nueva partida para: " + userEmail);
            SudokuGame newGame = generateNewBoard(user, difficulty, gameMode, null);
            return sudokuGameRepository.save(newGame);
        }
    }

    /**
     * Un método "helper" privado para crear (o actualizar) un tablero.
     * Si 'gameToUpdate' no es null, actualiza ese objeto.
     * Si es null, crea uno nuevo.
     */
    private SudokuGame generateNewBoard(User user, String difficulty, String gameMode, SudokuGame gameToUpdate) {

        // 1. Llama a tu generador para crear un tablero y su solución
        GeneratedPuzzle puzzle = generateSudoku(difficulty);

        // 2. Asigna los strings generados
        String newBoard = puzzle.boardString;
        String newSolution = puzzle.solutionString;
        // ------------------------------------

        SudokuGame game = (gameToUpdate != null) ? gameToUpdate : new SudokuGame();

        // --- Lógica de Tiempo (sigue igual) ---
        long timeLimit = 0L;
        if ("TIMED".equals(gameMode)) {
            if ("EASY".equals(difficulty)) timeLimit = 600L; // 10 minutos
            if ("MEDIUM".equals(difficulty)) timeLimit = 360; // 6 minutos
            if ("HARD".equals(difficulty)) timeLimit = 180L;// 3 minutos
        }

        // ... (Establece las propiedades del juego)
        game.setUser(user);
        game.setBoardString(newBoard);
        game.setSolutionString(newSolution);
        game.setDifficulty(difficulty);
        game.setState("IN_PROGRESS");
        game.setGameMode(gameMode);
        game.setTimeLimitSeconds(timeLimit);
        game.setTimeElapsedSeconds(0L);
        game.setLastUpdatedAt(LocalDateTime.now());

        return game;
    }

    // --- ¡¡INICIO DE TU ALGORITMO!! ---
    // (Pegado aquí como métodos 'private')

    private static final int BOARD_SIZE = 9;
    private static final int SUB_GRID_SIZE = 3;

    /**
     * ¡NUEVO! Clase "helper" para devolver ambos tableros.
     */
    private static class GeneratedPuzzle {
        String boardString;
        String solutionString;
        GeneratedPuzzle(String b, String s) { boardString = b; solutionString = s; }
    }

    /**
     * ¡Tu método 'generateSudoku', modificado para que:
     * 1. Acepte 'difficulty'
     * 2. Devuelva nuestra clase 'GeneratedPuzzle' (con los dos strings)
     */
    public GeneratedPuzzle generateSudoku(String difficulty) {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

        // 1. Rellena el tablero con una solución completa
        populateBoard(board);

        // 2. Guarda la solución (¡antes de borrar!)
        String solutionString = gridToString(board);

        // 3. Define cuántas celdas borrar según la dificultad
        int numRemoves = 40; // Medio
        if ("EASY".equals(difficulty)) numRemoves = 35; // Menos borrados = más fácil
        if ("HARD".equals(difficulty)) numRemoves = 45; // Más borrados = más difícil

        // 4. "Agujerea" el tablero
        removeCells(board, numRemoves);

        // 5. Convierte el tablero "agujereado" a string
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

        Random rand = new Random();
        for (int num : rand.ints(1, BOARD_SIZE + 1).distinct().limit(BOARD_SIZE).toArray()) {
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
     * Tu método 'removeCells', ¡modificado para aceptar 'numRemoves'!
     */
    private static void removeCells(int[][] board, int numRemoves) {
        Random rand = new Random();
        for (int i = 0; i < numRemoves; i++) {
            int row = rand.nextInt(BOARD_SIZE);
            int col = rand.nextInt(BOARD_SIZE);
            // Asegura que no borremos una celda ya borrada
            if (board[row][col] != 0) {
                board[row][col] = 0;
            } else {
                i--; // Inténtalo de nuevo
            }
        }
    }

    /**
     * ¡Nuestro "helper" para convertir tu int[][] a String!
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

    // --- MÉTODO PARA GUARDAR PROGRESO! ---
    public void saveGameProgress(String userEmail, SaveGameRequest request) {

        // 1. Busca al jugador
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // 2. Busca su partida "IN_PROGRESS"
        SudokuGame game = sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .orElseThrow(() -> new IllegalStateException("No se encontró partida en progreso para guardar"));

        // 3. ¡Actualiza los datos!
        game.setBoardString(request.getBoardString());
        game.setTimeElapsedSeconds(request.getTimeElapsedSeconds());
        game.setLastUpdatedAt(LocalDateTime.now()); // ¡Actualiza la "última vez tocado"!

        // 4. Guarda los cambios en la BBDD
        sudokuGameRepository.save(game);
    }

    // --- ¡NUEVO MÉTODO PARA COMPROBAR SOLUCIÓN! ---
    public boolean completeGame(String userEmail, SudokuSolutionRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        SudokuGame game = sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .orElseThrow(() -> new IllegalStateException("No se encontró partida en progreso para completar"));

        // ¡LA VALIDACIÓN!
        boolean isCorrect = game.getSolutionString().equals(request.getBoardString());

        if (!isCorrect) {
            System.out.println("Intento de completar fallido para: " + userEmail);
            return false; // No ganó
        }

        // ¡LA SOLUCIÓN ES CORRECTA!
        System.out.println("¡Partida completada exitosamente por: " + userEmail);

        if ("TIMED".equals(game.getGameMode())) {
            // Si era con tiempo, la guardamos para el "Scoreboard"
            game.setState("COMPLETED");
            // Actualiza el tiempo del juego con el tiempo final que envió el frontend
            game.setTimeElapsedSeconds(request.getTimeElapsedSeconds());
            game.setLastUpdatedAt(LocalDateTime.now());
            sudokuGameRepository.save(game);
        } else {
            // Si era "Libre", la borramos.
            sudokuGameRepository.delete(game);
        }

        return true; // ¡Ganó!
    }

    /**
     *
     * Marca la partida "IN_PROGRESS" de un usuario como "FAILED".
     */
    public void failGame(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Busca la partida en progreso
        sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .ifPresent(game -> {
                    // ¡La encontramos! La marcamos como fallida.
                    game.setState("FAILED");
                    game.setLastUpdatedAt(LocalDateTime.now());
                    sudokuGameRepository.save(game);
                    System.out.println("Partida marcada como FAILED para: " + userEmail);
                });
        // Si no se encuentra (ifPresent), no hace nada.
    }
    /**
     *
     * Obtiene el "Scoreboard" de un usuario.
     */
    public List<SudokuGame> getScoreboard(String userEmail) {

        // 1. Busca al jugador
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // 2. ¡Llama al método que ya teníamos en el Repositorio!
        // Le pide a la BBDD todas las partidas "COMPLETED" (que solo
        // pueden ser de modo 'TIMED', gracias a nuestra lógica)
        // y las ordena por tiempo.
        return sudokuGameRepository.findByUserAndStateOrderByTimeElapsedSecondsAsc(user, "COMPLETED");
    }

}
