package com.example.geoquiz.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un score dans la base de données.
 */
@Entity
@Table(name = "scores")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private int score;
}
