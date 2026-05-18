package com.example.geoquiz.controller;

import com.example.geoquiz.entity.Score;
import com.example.geoquiz.service.ScoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour exposer les endpoints de gestion des scores.
 */
@RestController
@RequestMapping("/api")
public class ScoreController {

    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    /**
     * Enregistrer un nouveau score.
     * POST http://localhost:8080/api/scores
     */
    @PostMapping("/scores")
    public Score addScore(@RequestBody Score score) {
        return service.saveScore(score);
    }

    /**
     * Récupérer les 10 meilleurs scores.
     * GET http://localhost:8080/api/leaderboard
     */
    @GetMapping("/leaderboard")
    public List<Score> getLeaderboard() {
        return service.getLeaderboard();
    }
}
