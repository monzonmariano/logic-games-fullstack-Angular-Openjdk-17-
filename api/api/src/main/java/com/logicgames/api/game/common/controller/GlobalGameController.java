package com.logicgames.api.game.common.controller;

import com.logicgames.api.game.common.dto.ScoreboardEntryDTO;
import com.logicgames.api.game.common.service.GlobalGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/games") // Ruta genérica para cosas compartidas
@RequiredArgsConstructor
public class GlobalGameController {

    private final GlobalGameService globalGameService;

    @GetMapping("/scoreboard")
    public ResponseEntity<List<ScoreboardEntryDTO>> getUnifiedScoreboard(Principal principal) {
        return ResponseEntity.ok(globalGameService.getUnifiedScoreboard(principal.getName()));
    }
}
