package com.example.geoquiz.repository;

import com.example.geoquiz.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    /**
     * Récupère les 10 meilleurs scores triés par ordre décroissant.
     * Spring Data JPA génère automatiquement la requête SQL.
     */
    List<Score> findTop10ByOrderByScoreDesc();
}
