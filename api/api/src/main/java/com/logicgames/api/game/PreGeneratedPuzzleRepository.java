package com.logicgames.api.game;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PreGeneratedPuzzleRepository extends  JpaRepository<PreGeneratedPuzzle, Long>{

    /**
     * SOLUCIÓN RACE CONDITION:
     * Usamos una consulta nativa de PostgreSQL con "FOR UPDATE SKIP LOCKED".
     * Esto busca un puzzle, lo bloquea atómicamente y salta los que estén siendo usados por otros.
     */
    @Query(value = "SELECT * FROM pre_generated_puzzles WHERE difficulty = :difficulty LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<PreGeneratedPuzzle> findAnyAvailableByDifficulty(@Param("difficulty") String difficulty);

    /**
     * ¡El método del JOB!
     * Borra puzzles pre-generados que son más viejos que la fecha límite.
     */
    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime cutOffDate);
}
