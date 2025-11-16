package com.logicgames.api.game;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class GameCleanupService {

    private final SudokuGameRepository sudokuGameRepository;
    private final PreGeneratedPuzzleRepository puzzleRepository;
    private final GameMetricRepository metricRepository;
    private final SudokuGeneratorService generatorService;
    /**
     * ¡TU JOB!
     * Se ejecuta automáticamente basado en la expresión "cron".
     *
     * "cron = 0 * * * * ?"  = "En el segundo 0 de cada minuto" (PARA PRUEBAS)
     * "cron = 0 0 3 * * ?"  = "A las 3:00 AM, todos los días" (PARA PRODUCCIÓN)
     */

    /**
     * ¡TU JOB "ELÁSTICO" DE PRODUCCIÓN!
     * Se ejecuta "En el minuto 0 de cada hora" (ej. 13:00, 14:00, 15:00)
     */
    @Scheduled(cron = "0 0 * * * ?") // Se ejecuta cada hora
    public void elasticCleanupJob() {
        System.out.println("-> JOB ELÁSTICO: Iniciando ejecución...");

        // --- 1. Limpieza de FAILED ---
        LocalDateTime failedCutOff = LocalDateTime.now().minusDays(7);
        sudokuGameRepository.deleteByStateAndLastUpdatedAtBefore("FAILED", failedCutOff);
        System.out.println("-> JOB ELÁSTICO: Partidas 'FAILED' antiguas borradas.");

        // --- 2. Lógica Elástica del Pool  ---

        // 2a. Define límites
        long maxPoolSize = 40; // Tu límite "máximo"
        long minPoolSize = 5;  // ¡Tu nuevo límite "mínimo"!
        long lowDemandThreshold = 10;

        // 2b. Mide el estado actual
        long currentPoolSize = puzzleRepository.count();
        long demandLastHour = metricRepository.countByEventTypeAndEventTimestampAfter(
                "PUZZLE_CONSUMED_EASY", LocalDateTime.now().minusHours(1)) +
                metricRepository.countByEventTypeAndEventTimestampAfter(
                        "PUZZLE_CONSUMED_MEDIUM", LocalDateTime.now().minusHours(1)) +
                metricRepository.countByEventTypeAndEventTimestampAfter(
                        "PUZZLE_CONSUMED_HARD", LocalDateTime.now().minusHours(1)
                );

        System.out.println("-> JOB ELÁSTICO: (Pool: " + currentPoolSize + " / Demanda(1h): " + demandLastHour + ")");

        // 2c. ¡Toma la decisión!

        // --- ESCALA HACIA ABAJO ---
        if (currentPoolSize > maxPoolSize && demandLastHour < lowDemandThreshold) {

            System.out.println("-> JOB ELÁSTICO: ¡Sobreabastecimiento detectado! Purgando puzzles viejos...");
            LocalDateTime poolCutOff = LocalDateTime.now().minusHours(1);
            puzzleRepository.deleteByCreatedAtBefore(poolCutOff);

            // --- ¡NUEVA LÓGICA DE ESCALA HACIA ARRIBA !---
        } else if (currentPoolSize < minPoolSize) {

            // ¡NOS ESTAMOS QUEDANDO SIN PUZZLES!
            System.out.println("-> JOB ELÁSTICO: ¡Pool bajo detectado! Rellenando proactivamente...");

            // Llama al "Ayudante" asíncrono para que rellene
            // (El Ayudante ya es "inteligente" y generará 10 o 20
            // basado en la demanda de la última hora)
            generatorService.populatePoolAsync("EASY");
            generatorService.populatePoolAsync("MEDIUM");
            generatorService.populatePoolAsync("HARD");

        } else {
            System.out.println("-> JOB ELÁSTICO: El tamaño del pool es saludable.");
        }

        System.out.println("-> JOB ELÁSTICO: Ejecución completada.");
    }

    // --- EJECUCIÓN DIARIA (PARA PRODUCCIÓN) ---
    // (Descomenta esta línea cuando estés listo)
    /*
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldGames_Production() {
        System.out.println("-> JOB: Ejecutando Job de Limpieza (Producción)");

        // 1. Borra partidas FAILED de hace más de 7 días
        LocalDateTime failedGamesCutOff = LocalDateTime.now().minusDays(7);
        sudokuGameRepository.deleteByStateAndLastUpdatedAtBefore("FAILED", failedGamesCutOff);

        // 2. Borra puzzles del pool de hace más de 1 día (para que siempre estén frescos)
        LocalDateTime puzzlePoolCutOff = LocalDateTime.now().minusDays(1);
        puzzleRepository.deleteByCreatedAtBefore(puzzlePoolCutOff);
    }
    */


}
