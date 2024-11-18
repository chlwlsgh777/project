package com.gamesearch.domain.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsByAppId(Long appId);

    boolean existsById(Long id);

    Game findByName(String name);

    Game findByAppId(Long appId);
}