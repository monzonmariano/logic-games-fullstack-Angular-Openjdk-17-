package com.logicgames.api.game;


import com.logicgames.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository // Post-it: "Soy un Archivador (Acceso a BBDD)"
public interface SudokuGameRepository extends  JpaRepository<SudokuGame,Long>{

    // --- Pregunta 1: Para el botón "Retomar Partida" ---
    // "Busca una partida por su 'User' (dueño) Y por su 'state'"
    Optional<SudokuGame> findByUserAndState(User user, String state);

    // --- Pregunta 2: Para el "Historial / Scoreboard" ---
    // "Busca TODAS las partidas por 'User' Y 'state',
    // y ordénalas por 'timeElapsedSeconds' de menor a mayor"
    List<SudokuGame> findByUserAndStateOrderByTimeElapsedSecondsAsc(User user, String state);



    /**
     * ¡El método del JOB!
     * Borra partidas que cumplan DOS condiciones:
     * 1. Su estado es el que le pasemos (ej. "FAILED").
     * 2. Su última actualización fue ANTES de la fecha límite.
     */
    @Modifying
    @Transactional
    void deleteByStateAndLastUpdatedAtBefore(String state, LocalDateTime cutOffDate);

}
