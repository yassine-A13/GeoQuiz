package com.example.geoquiz.service;

import com.example.geoquiz.entity.Score;
import com.example.geoquiz.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service gérant la logique métier des scores.
 */
@Service
public class ScoreService {

    private final ScoreRepository repository;

    public ScoreService(ScoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Enregistre un nouveau score en base de données.
     */
    public Score saveScore(Score score) {
        return repository.save(score);
    }

    /**
     * Récupère le top 10 des meilleurs scores.
     */
    public List<Score> getLeaderboard() {
        return repository.findTop10ByOrderByScoreDesc();
    }
}
