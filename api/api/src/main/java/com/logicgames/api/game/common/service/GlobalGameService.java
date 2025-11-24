package com.logicgames.api.game.common.service;

import com.logicgames.api.game.common.dto.ScoreboardEntryDTO;
import com.logicgames.api.game.sudoku.model.SudokuGame;
import com.logicgames.api.game.sudoku.repository.SudokuGameRepository;
import com.logicgames.api.game.wordsearch.model.WordSearchGame;
import com.logicgames.api.game.wordsearch.repository.WordSearchGameRepository;
import com.logicgames.api.user.model.User;
import com.logicgames.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalGameService {

    private final UserRepository userRepository;
    private final SudokuGameRepository sudokuRepository;
    private final WordSearchGameRepository wordSearchRepository;

    @Transactional(readOnly = true)
    public List<ScoreboardEntryDTO> getUnifiedScoreboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        List<ScoreboardEntryDTO> unifiedList = new ArrayList<>();

        // 1. Buscar Sudokus Completados
        List<SudokuGame> sudokus = sudokuRepository.findByUserAndStateOrderByTimeElapsedSecondsAsc(user, "COMPLETED");
        unifiedList.addAll(sudokus.stream().map(game -> ScoreboardEntryDTO.builder()
                .id(game.getId())
                .gameType("SUDOKU") // Marcamos el tipo
                .difficulty(game.getDifficulty())
                .timeElapsedSeconds(game.getTimeElapsedSeconds())
                .lastUpdatedAt(game.getLastUpdatedAt())
                .userEmail(user.getEmail())
                .build()).toList());

        // 2. Buscar Sopas de Letras Completadas
        List<WordSearchGame> wordSearches = wordSearchRepository.findByUserAndStateOrderByTimeElapsedSecondsAsc(user, "COMPLETED");
        unifiedList.addAll(wordSearches.stream().map(game -> ScoreboardEntryDTO.builder()
                .id(game.getId())
                .gameType("WORD_SEARCH") // Marcamos el tipo
                .difficulty(game.getDifficulty())
                .timeElapsedSeconds(game.getTimeElapsedSeconds())
                .lastUpdatedAt(game.getLastUpdatedAt())
                .userEmail(user.getEmail())
                .build()).toList());

        // 3. Ordenar la lista mezclada (Por fecha más reciente o por tiempo, tú eliges)
        // Aquí ordenamos por fecha de finalización (lo más nuevo arriba)
        unifiedList.sort(Comparator.comparing(ScoreboardEntryDTO::getLastUpdatedAt).reversed());

        return unifiedList;
    }
}
