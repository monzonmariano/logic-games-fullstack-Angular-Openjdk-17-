package com.logicgames.api.game.wordsearch.repository;


import com.logicgames.api.game.wordsearch.model.WordSearchGame;
import com.logicgames.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordSearchGameRepository extends JpaRepository<WordSearchGame, Long> {

    // Buscar partida activa del usuario
    Optional<WordSearchGame> findByUserAndState(User user, String state);

    @Modifying
    @Transactional
    void deleteByStateAndLastUpdatedAtBefore(String state, LocalDateTime cutOffDate);

    List<WordSearchGame> findByUserAndStateOrderByTimeElapsedSecondsAsc(User user, String state);
}
