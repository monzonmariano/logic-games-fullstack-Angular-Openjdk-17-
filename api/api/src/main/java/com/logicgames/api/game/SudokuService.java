package com.logicgames.api.game;




import com.logicgames.api.user.User;
import com.logicgames.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.logicgames.api.game.dtos.SudokuSolutionRequest;
import com.logicgames.api.game.dtos.SaveGameRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.logicgames.api.game.dtos.ScoreboardEntryDTO;
import java.util.stream.Collectors;


@Service // Post-it: "Soy un Cerebro (Lógica de Negocio)"
@RequiredArgsConstructor // Post-it: ¡Crea mi constructor para inyectar mis herramientas!
public class SudokuService {


    // El Archivador de Sudoku (para buscar/guardar partidas)
    private final SudokuGameRepository sudokuGameRepository;
    // El Archivador de User (para encontrar al jugador)
    private final UserRepository userRepository;
    // Para las metricas
    private final GameMetricRepository metricRepository;

    // --- 1. Las Herramientas que necesitamos ---
    private final PreGeneratedPuzzleRepository puzzleRepository;
    private final SudokuGeneratorService generatorService;



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
            if (game.getDifficulty().equals(difficulty) && game.getGameMode().equals(gameMode)) {
                System.out.println("Partida en progreso encontrada para: " + userEmail);
                return game;
            }
            System.out.println("Sobrescribiendo partida en progreso para: " + userEmail);
            // ¡Llama al método HELPER actualizado!
            SudokuGame updatedGame = generateNewBoard(user, difficulty, gameMode, game);
            return sudokuGameRepository.save(updatedGame);

        } else {
            System.out.println("Creando nueva partida para: " + userEmail);
            // ¡Llama al método HELPER actualizado!
            SudokuGame newGame = generateNewBoard(user, difficulty, gameMode, null);
            return sudokuGameRepository.save(newGame);
        }
    }

    private SudokuGame generateNewBoard(User user, String difficulty, String gameMode, SudokuGame gameToUpdate) {

        String newBoard;
        String newSolution;

        // 1. Intenta sacar un puzzle del pool
        Optional<PreGeneratedPuzzle> puzzleOpt = puzzleRepository.findTopByDifficulty(difficulty);

        if (puzzleOpt.isPresent()) {
            // ¡Genial! El pool tenía uno.
            PreGeneratedPuzzle puzzle = puzzleOpt.get();
            System.out.println("-> POOL: Puzzle encontrado en el pool para " + difficulty);
            newBoard = puzzle.getBoardString();
            newSolution = puzzle.getSolutionString();
            puzzleRepository.delete(puzzle); // ¡Importante! Bórralo del pool.
            GameMetric metric = GameMetric.builder()
                    .eventType("PUZZLE_CONSUMED_" + difficulty)
                    .build();
            metricRepository.save(metric);

        } else {
            // ¡El pool está vacío! (El servidor acaba de despertar)
            System.out.println("-> POOL: Pool vacío para " + difficulty + ". Generando uno SÍNCRONAMENTE.");

            // 2. Genera UNO síncronamente para este usuario (Llama al nuevo servicio)
            var puzzle = generatorService.generateSudokuSync(difficulty); // Llama al método 'Sync'
            newBoard = puzzle. boardString;
            newSolution = puzzle.solutionString;

            GameMetric metric = GameMetric.builder()
                    .eventType("POOL_EMPTY_" + difficulty)
                    .build();
            metricRepository.save(metric);

            // 3. ¡Lanza la tarea ASÍNCRONA para rellenar el pool!
            generatorService.populatePoolAsync(difficulty);
        }

        // --- (El resto del método sigue igual que antes) ---
        SudokuGame game = (gameToUpdate != null) ? gameToUpdate : new SudokuGame();

        long timeLimit = 0L;
        if ("TIMED".equals(gameMode)) {
            if ("EASY".equals(difficulty)) timeLimit = 600L;
            if ("MEDIUM".equals(difficulty)) timeLimit = 360;
            if ("HARD".equals(difficulty)) timeLimit = 180L;
        }

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

    public void saveGameProgress(String userEmail, SaveGameRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        SudokuGame game = sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .orElseThrow(() -> new IllegalStateException("No se encontró partida en progreso para guardar"));
        game.setBoardString(request.getBoardString());
        game.setTimeElapsedSeconds(request.getTimeElapsedSeconds());
        game.setLastUpdatedAt(LocalDateTime.now());
        sudokuGameRepository.save(game);
    }

    public boolean completeGame(String userEmail, SudokuSolutionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        SudokuGame game = sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .orElseThrow(() -> new IllegalStateException("No se encontró partida en progreso para completar"));
        boolean isCorrect = game.getSolutionString().equals(request.getBoardString());
        if (!isCorrect) {
            System.out.println("Intento de completar fallido para: " + userEmail);
            return false;
        }
        System.out.println("¡Partida completada exitosamente por: " + userEmail);
        if ("TIMED".equals(game.getGameMode())) {
            game.setState("COMPLETED");
            game.setTimeElapsedSeconds(request.getTimeElapsedSeconds());
            game.setLastUpdatedAt(LocalDateTime.now());
            sudokuGameRepository.save(game);
        } else {
            sudokuGameRepository.delete(game);
        }
        return true;
    }

    public void failGame(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        sudokuGameRepository.findByUserAndState(user, "IN_PROGRESS")
                .ifPresent(game -> {
                    game.setState("FAILED");
                    game.setLastUpdatedAt(LocalDateTime.now());
                    sudokuGameRepository.save(game);
                    System.out.println("Partida marcada como FAILED para: " + userEmail);
                });
    }

    public List<ScoreboardEntryDTO> getScoreboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        List<SudokuGame> completedGames = sudokuGameRepository
                .findByUserAndStateOrderByTimeElapsedSecondsAsc(user, "COMPLETED");
        return completedGames.stream()
                .map(game -> ScoreboardEntryDTO.builder()
                        .id(game.getId())
                        .difficulty(game.getDifficulty())
                        .timeElapsedSeconds(game.getTimeElapsedSeconds())
                        .lastUpdatedAt(game.getLastUpdatedAt())
                        .userEmail(game.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());
    }

}
