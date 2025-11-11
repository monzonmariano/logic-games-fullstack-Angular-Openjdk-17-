package com.logicgames.api.game;


import com.logicgames.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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



    // --- Pregunta 3: Para tu "Tarea de Limpieza" (¡del futuro!) ---
    // "Borra todas las partidas cuyo 'state' sea X Y
    // cuya 'lastUpdatedAt' sea anterior a (Before) la fecha Y"
    // (Este método necesita una anotación @Modifying y @Transactional,
    // pero lo añadiremos cuando hagamos el servicio de limpieza)
    // void deleteByStateAndLastUpdatedAtBefore(String state, LocalDateTime cutOffDate);
}
