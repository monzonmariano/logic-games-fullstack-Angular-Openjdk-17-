package com.logicgames.api.game.wordsearch.service;

import com.logicgames.api.game.common.repository.GameMetricRepository;
import com.logicgames.api.game.wordsearch.model.PreGeneratedWordSearch;
import com.logicgames.api.game.wordsearch.repository.PreGeneratedWordSearchRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class WordSearchGeneratorService {

    private final PreGeneratedWordSearchRepository poolRepository;
    private final GameMetricRepository metricRepository;
    private final WordDictionaryService dictionaryService;

    private static final Random RANDOM = new Random();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // DTO de salida del generador
    @Data
    public static class GeneratedWordSearch {
        private String matrixString;
        private List<String> wordsToFind;
        private int gridSize;

        public GeneratedWordSearch(String matrixString, List<String> wordsToFind, int gridSize) {
            this.matrixString = matrixString;
            this.wordsToFind = wordsToFind;
            this.gridSize = gridSize;
        }
    }

    /**
     * Genera una partida en memoria (Algoritmo Backtracking).
     */
    public GeneratedWordSearch generateGame(int gridSize, List<String> wordsInput) {
        // 1. Crear matriz vacía
        char[][] grid = new char[gridSize][gridSize];

        // 2. Ordenar palabras por longitud (las largas son más difíciles de ubicar)
        List<String> wordsToPlace = new ArrayList<>(wordsInput);
        wordsToPlace.sort((a, b) -> b.length() - a.length());

        List<String> placedWords = new ArrayList<>();

        // 3. Intentar colocar cada palabra
        for (String word : wordsToPlace) {
            word = word.toUpperCase();
            if (placeWord(grid, word, gridSize)) {
                placedWords.add(word);
            } else {
                // System.out.println("⚠️ No cupo: " + word); // (Debug opcional)
            }
        }

        // 4. Rellenar espacios vacíos
        fillEmptySpaces(grid, gridSize);

        // 5. Convertir a String
        String matrixString = convertGridToString(grid);
        Collections.shuffle(placedWords);

        return new GeneratedWordSearch(matrixString, placedWords, gridSize);
    }

    /**
     * Tarea Asíncrona: Rellena la tabla de 'pre_generated_word_search'.
     */
    @Async
    public void populatePoolAsync(String difficulty, String theme) {
        System.out.println("-> TAREA ASÍNCRONA (WS): Rellenando pool para " + difficulty + " [" + theme + "]");

        // Configuración Dificultad (Debe coincidir con la del Service principal)
        int gridSize = 10;
        int wordCount = 5;
        if ("MEDIUM".equals(difficulty)) { gridSize = 12; wordCount = 8; }
        if ("HARD".equals(difficulty)) { gridSize = 15; wordCount = 12; }

        int batchSize = 3; // Generamos 3 de golpe para no saturar

        try {
            for (int i = 0; i < batchSize; i++) {

                // 1. Obtener palabras del servicio de diccionario
                // Pedimos el doble (wordCount * 2) para tener reservas si alguna no cabe
                List<String> selected = dictionaryService.getWords(theme, difficulty, wordCount * 2);

                // 2. Generar la matriz
                GeneratedWordSearch gen = this.generateGame(gridSize, selected);

                // 3. Guardar en BBDD
                PreGeneratedWordSearch preGame = PreGeneratedWordSearch.builder()
                        .difficulty(difficulty)
                        .gridSize(gridSize)
                        .matrixString(gen.getMatrixString())
                        .wordsToFindString(String.join(",", gen.getWordsToFind()))
                        .theme(theme)
                        .build();

                poolRepository.save(preGame);
            }
            System.out.println("-> TAREA ASÍNCRONA (WS): Pool actualizado correctamente.");
        } catch (Exception e) {
            System.err.println("Error generando pool WS: " + e.getMessage());
        }
    }

    // --- ALGORITMO DE COLOCACIÓN (Privado) ---

    private boolean placeWord(char[][] grid, String word, int size) {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            attempts++;

            // 0: Horizontal, 1: Vertical, 2: Diagonal
            int direction = RANDOM.nextInt(3);
            int row = RANDOM.nextInt(size);
            int col = RANDOM.nextInt(size);

            int dRow = 0, dCol = 0;
            if (direction == 0) dCol = 1;
            else if (direction == 1) dRow = 1;
            else { dRow = 1; dCol = 1; }

            if (canPlace(grid, word, row, col, dRow, dCol, size)) {
                for (int i = 0; i < word.length(); i++) {
                    grid[row + i * dRow][col + i * dCol] = word.charAt(i);
                }
                return true;
            }
        }
        return false;
    }

    private boolean canPlace(char[][] grid, String word, int row, int col, int dRow, int dCol, int size) {
        int lastRow = row + (word.length() - 1) * dRow;
        int lastCol = col + (word.length() - 1) * dCol;

        if (lastRow >= size || lastCol >= size) return false;

        for (int i = 0; i < word.length(); i++) {
            char currentCharInGrid = grid[row + i * dRow][col + i * dCol];
            char charInWord = word.charAt(i);

            if (currentCharInGrid != 0 && currentCharInGrid != charInWord) {
                return false; // Choque con letra diferente
            }
        }
        return true;
    }

    private void fillEmptySpaces(char[][] grid, int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 0) {
                    grid[i][j] = ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
                }
            }
        }
    }

    private String convertGridToString(char[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(row);
        }
        return sb.toString();
    }
}
