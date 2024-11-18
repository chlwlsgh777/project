package com.gamesearch.domain.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @PostConstruct
    public void init() throws Exception {
        // 초기화 로직 (JSON 파일에서 데이터 읽기 등)
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game findByName(String name) {
        return gameRepository.findByName(name);
    }

    public Game findById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    public long getGameCount() {
        return gameRepository.count(); // 게임 수 반환
    }

    public Game findByAppId(Long appId) {
        return gameRepository.findByAppId(appId);
    }
}