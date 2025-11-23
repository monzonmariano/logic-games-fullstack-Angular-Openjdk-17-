package com.logicgames.api.game.wordsearch.service;


import com.logicgames.api.game.common.model.GameMetric;
import com.logicgames.api.game.common.repository.GameMetricRepository;
import com.logicgames.api.game.wordsearch.model.PreGeneratedWordSearch;
import com.logicgames.api.game.wordsearch.model.WordSearchGame;
import com.logicgames.api.game.wordsearch.repository.PreGeneratedWordSearchRepository;
import com.logicgames.api.game.wordsearch.repository.WordSearchGameRepository;
import com.logicgames.api.user.model.User;
import com.logicgames.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class WordSearchService {

    private final WordSearchGameRepository gameRepository;
    private final UserRepository userRepository;
    private final WordSearchGeneratorService generatorService;

    // Nuevas dependencias para el Pool y el Diccionario
    private final PreGeneratedWordSearchRepository poolRepository;
    private final GameMetricRepository metricRepository;
    private final WordDictionaryService dictionaryService; // <--- ¡LA INYECCIÓN CLAVE!

    public WordSearchGame loadOrCreateGame(String userEmail, String difficulty, String gameMode, String theme) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        // 1. Buscar partida existente
        Optional<WordSearchGame> activeGame = gameRepository.findByUserAndState(user, "IN_PROGRESS");
        if (activeGame.isPresent()) {
            WordSearchGame game = activeGame.get();

            // CORRECCIÓN DEL BUG:
            // Solo retomamos si coincide Dificultad, Modo Y TEMA.
            if (game.getDifficulty().equals(difficulty)
                    && game.getGameMode().equals(gameMode)
                    && game.getTheme().equals(theme)) {

                return game;
            }

            // Si no coincide (ej. quería Tech y tengo Animales), borramos la vieja.
            gameRepository.delete(game);
        }

        return createNewGameFromPool(user, difficulty, gameMode, theme);
    }

    private WordSearchGame createNewGameFromPool(User user, String difficulty, String gameMode, String theme) {
        String matrix;
        List<String> words;
        int gridSize;

        // 1. INTENTAR SACAR DEL POOL (Buscamos por Dificultad Y Tema)
        // Nota: Asumimos que actualizaste el repositorio para aceptar 'theme'
        // Si no, usa findAnyAvailableByDifficulty y filtra en memoria o acepta cualquiera
        // Idealmente: poolRepository.findAnyAvailable(difficulty, theme);

        // Por ahora, usaremos el método básico y si el tema no coincide mala suerte (o actualizas el Repo)
        // Vamos a suponer que el repositorio YA TIENE el método findAnyAvailable(difficulty, theme)
        // Si no te compila esto, usa findAnyAvailableByDifficulty(difficulty) temporalmente.

        // Para ser consistentes con tu código anterior, usaré la búsqueda simple y asumiré
        // que el pool se llenará con el tema solicitado en segundo plano.

        // IMPORTANTE: Si implementaste el cambio en el repositorio:
        // Optional<PreGeneratedWordSearch> poolOpt = poolRepository.findAnyAvailable(difficulty, theme);

        // Si NO lo implementaste aún, usa este fallback:
        Optional<PreGeneratedWordSearch> poolOpt = Optional.empty(); // Forzamos generación real para probar el diccionario

        if (poolOpt.isPresent()) {
            System.out.println("-> WS POOL: ¡Juego encontrado en caché!");
            PreGeneratedWordSearch cached = poolOpt.get();
            matrix = cached.getMatrixString();
            words = Arrays.asList(cached.getWordsToFindString().split(","));
            gridSize = cached.getGridSize();

            poolRepository.delete(cached);
            metricRepository.save(GameMetric.builder().eventType("WS_CONSUMED_" + difficulty).build());
        } else {
            System.out.println("-> WS POOL: Vacío. Generando en tiempo real.");

            // Configuración Dificultad
            int size = 10;
            int count = 5;
            if ("MEDIUM".equals(difficulty)) { size = 12; count = 8; }
            if ("HARD".equals(difficulty)) { size = 15; count = 12; }

            // --- USO DEL DICCIONARIO DINÁMICO ---
            // Pedimos palabras al servicio (el servicio ya sabe si ir al JSON o a la lista fija)
            // Pedimos el doble (count * 2) para tener reservas si alguna no cabe en la matriz
            List<String> selectedWords = dictionaryService.getWords(theme, difficulty, count * 2);

            // Generar Matriz
            var gen = generatorService.generateGame(size, selectedWords);
            matrix = gen.getMatrixString();
            words = gen.getWordsToFind();
            gridSize = size;

            // Lanzar recarga asíncrona para el futuro
            generatorService.populatePoolAsync(difficulty, theme); // <--- Pasamos el tema
        }

        // 2. CONFIGURAR TIEMPO
        long timeLimit = 0;
        if ("TIMED".equals(gameMode)) {
            if ("EASY".equals(difficulty)) timeLimit = 600;
            else if ("MEDIUM".equals(difficulty)) timeLimit = 360;
            else if ("HARD".equals(difficulty)) timeLimit = 180;
        }

        // 3. GUARDAR
        WordSearchGame game = WordSearchGame.builder()
                .user(user)
                .state("IN_PROGRESS")
                .difficulty(difficulty)
                .gameMode(gameMode)
                .theme(theme)
                .gridSize(gridSize)
                .matrixString(matrix)
                .wordsToFind(words)
                .foundWords(new HashSet<>())
                .timeLimitSeconds(timeLimit)
                .timeElapsedSeconds(0L)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        return gameRepository.save(game);
    }

    public void saveProgress(String userEmail, List<String> foundWords, Long timeSeconds, boolean isComplete) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        WordSearchGame game = gameRepository.findByUserAndState(user, "IN_PROGRESS")
                .orElseThrow(() -> new IllegalStateException("No hay partida activa"));

        game.setFoundWords(new HashSet<>(foundWords));
        game.setTimeElapsedSeconds(timeSeconds);
        game.setLastUpdatedAt(LocalDateTime.now());

        if (isComplete) {
            if (game.getFoundWords().containsAll(game.getWordsToFind())) {
                game.setState("COMPLETED");
            }
        }
        gameRepository.save(game);
    }

    public void failGame(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        // Buscamos la partida activa
        gameRepository.findByUserAndState(user, "IN_PROGRESS")
                .ifPresent(game -> {
                    game.setState("FAILED"); // La marcamos como perdida/abandonada
                    game.setLastUpdatedAt(LocalDateTime.now());
                    gameRepository.save(game);
                });
    }
}