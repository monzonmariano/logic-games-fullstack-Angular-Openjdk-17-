package com.logicgames.api.game.wordsearch.repository;


import com.logicgames.api.game.wordsearch.model.PreGeneratedWordSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PreGeneratedWordSearchRepository extends JpaRepository<PreGeneratedWordSearch, Long>{

    // SKIP LOCKED: La solución anti-carrera para sacar un juego del pool atómicamente
    @Query(value = "SELECT * FROM pre_generated_word_search WHERE difficulty = :difficulty AND theme = :theme LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<PreGeneratedWordSearch> findAnyAvailable(@Param("difficulty") String difficulty, @Param("theme") String theme);



    // --- ¡NUEVO MÉTODO DE LIMPIEZA! ---
    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime cutOffDate);
}
