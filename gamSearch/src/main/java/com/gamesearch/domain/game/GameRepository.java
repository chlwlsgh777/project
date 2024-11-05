package com.gamesearch.domain.game;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsByAppId(Long appId);
    
}


