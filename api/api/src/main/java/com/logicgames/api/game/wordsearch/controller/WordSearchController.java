package com.logicgames.api.game.wordsearch.controller;


import com.logicgames.api.game.wordsearch.dto.WordSearchSaveRequest;
import com.logicgames.api.game.wordsearch.model.WordSearchGame;
import com.logicgames.api.game.wordsearch.service.WordSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/wordsearch")
@RequiredArgsConstructor
public class WordSearchController {

    private final WordSearchService wordSearchService;

    @GetMapping("/load-or-create")
    public ResponseEntity<WordSearchGame> loadOrCreateGame(
            Principal principal,
            @RequestParam(defaultValue = "EASY") String difficulty,
            @RequestParam(defaultValue = "FREE") String gameMode,
            @RequestParam(defaultValue = "GENERAL") String theme // <--- ¡FALTABA ESTO!
    ) {
        String userEmail = principal.getName();
        // Ahora pasamos los 4 argumentos
        WordSearchGame game = wordSearchService.loadOrCreateGame(userEmail, difficulty, gameMode, theme);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveGame(
            Principal principal,
            @RequestBody WordSearchSaveRequest request,
            @RequestParam(defaultValue = "false") boolean complete
    ) {
        String userEmail = principal.getName();
        wordSearchService.saveProgress(userEmail, request.getFoundWords(), request.getTimeElapsedSeconds(), complete);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> failGame(Principal principal) {
        String userEmail = principal.getName();
        wordSearchService.failGame(userEmail);
        return ResponseEntity.ok().build();
    }
}
