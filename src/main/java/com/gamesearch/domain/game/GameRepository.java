package com.gamesearch.domain.game;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsByAppId(Long appId);

    Optional<Game> findByName(String name); // 이름으로 게임 조회

    Optional<Game> findByAppId(Long appId); // App ID로 게임 조회
}