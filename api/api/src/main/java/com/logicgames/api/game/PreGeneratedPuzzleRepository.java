package com.logicgames.api.game;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Repository
public interface PreGeneratedPuzzleRepository extends  JpaRepository<PreGeneratedPuzzle, Long>{

    // "Busca el primer puzzle que encuentres para esta dificultad"
    Optional<PreGeneratedPuzzle> findTopByDifficulty(String difficulty);

    /**
     * ¡El método del JOB!
     * Borra puzzles pre-generados que son más viejos que la fecha límite.
     */
    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime cutOffDate);
}
