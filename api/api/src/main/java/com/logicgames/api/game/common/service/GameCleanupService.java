package com.logicgames.api.game.common.service;

import com.logicgames.api.game.common.repository.GameMetricRepository;
import com.logicgames.api.game.sudoku.repository.PreGeneratedPuzzleRepository;
import com.logicgames.api.game.sudoku.repository.SudokuGameRepository;
import com.logicgames.api.game.sudoku.service.SudokuGeneratorService;
// --- IMPORTS DE WORD SEARCH (Asegúrate de tenerlos) ---
import com.logicgames.api.game.wordsearch.repository.PreGeneratedWordSearchRepository;
import com.logicgames.api.game.wordsearch.repository.WordSearchGameRepository;
import com.logicgames.api.game.wordsearch.service.WordSearchGeneratorService;
// --- IMPORTS DE USUARIO ---
import com.logicgames.api.user.repository.UserRepository; // O el paquete correcto donde esté tu UserRepository
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameCleanupService {

    // --- SUDOKU ---
    private final SudokuGameRepository sudokuGameRepository;
    private final PreGeneratedPuzzleRepository sudokuPoolRepository;
    private final SudokuGeneratorService sudokuGenerator;

    // --- WORD SEARCH ---
    private final WordSearchGameRepository wordSearchGameRepository;
    private final PreGeneratedWordSearchRepository wordSearchPoolRepository;
    private final WordSearchGeneratorService wordSearchGenerator;

    // --- COMÚN ---
    private final GameMetricRepository metricRepository;
    private final UserRepository userRepository;

    /**
     * Job programado: Se ejecuta cada hora.
     * Limpia partidas viejas y usuarios no verificados.
     */
    @Scheduled(cron = "0 0 * * * ?") // Cada hora en punto
    @Transactional // Importante para que los 'delete' funcionen en bloque
    public void elasticCleanupJob() {
        System.out.println("-> JOB ELÁSTICO: Iniciando limpieza global...");

        LocalDateTime failedCutOff = LocalDateTime.now().minusDays(7);
        LocalDateTime abandonedCutOff = LocalDateTime.now().minusDays(30);
        LocalDateTime poolCutOff = LocalDateTime.now().minusDays(1);

        // 1. LIMPIEZA SUDOKU
        // Borra partidas fallidas antiguas
        sudokuGameRepository.deleteByStateAndLastUpdatedAtBefore("FAILED", failedCutOff);

        // 2. LIMPIEZA WORD SEARCH
        // Borra partidas fallidas o abandonadas
        wordSearchGameRepository.deleteByStateAndLastUpdatedAtBefore("FAILED", failedCutOff);
        wordSearchGameRepository.deleteByStateAndLastUpdatedAtBefore("IN_PROGRESS", abandonedCutOff);

        // 3. LIMPIEZA DE POOLS (Mantenimiento preventivo)
        // Borra puzzles pre-generados que nadie usó en 24h
        sudokuPoolRepository.deleteByCreatedAtBefore(poolCutOff);
        wordSearchPoolRepository.deleteByCreatedAtBefore(poolCutOff);

        // 4. USUARIOS NO VERIFICADOS
        // Borra usuarios que se registraron hace 7 días y nunca verificaron email
        // (Asegúrate de tener este método en UserRepository)
        userRepository.deleteByIsVerifiedFalseAndCreatedAtBefore(failedCutOff);
        // O si usaste 'createdAt':
        // userRepository.deleteByIsVerifiedFalseAndCreatedAtBefore(failedCutOff);

        System.out.println("-> JOB ELÁSTICO: Limpieza completada.");

        // --- AQUÍ PODRÍAS AÑADIR LÓGICA DE AUTO-ESCALADO (Opcional) ---
        // checkDemandAndRefillPools();
    }

    // Método auxiliar opcional para rellenar pools si están bajos
    private void checkDemandAndRefillPools() {
        // Lógica futura para llamar a wordSearchGenerator.populatePoolAsync(...)
    }
}