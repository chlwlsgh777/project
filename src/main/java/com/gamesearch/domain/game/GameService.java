package com.gamesearch.domain.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.io.File;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<Game> games = objectMapper.readValue(new File("src/main/resources/games/extracted_games_data.json"),
                new TypeReference<List<Game>>() {
                });

        // 데이터베이스에 저장
        for (Game game : games) {
            if (!gameRepository.existsByAppId(game.getAppId())) { // 중복 체크
                gameRepository.save(game);
            }
        }

    }

    // 모든 게임을 가져오는 메서드
    public List<Game> getAllGames() {
        return gameRepository.findAll(); // 게임 목록 반환
    }

    // 게임 수를 반환하는 메서드
    public long getGameCount() {
        return gameRepository.count(); // 게임 수 반환
    }

    public Game findByAppId(Long appId) {
        return gameRepository.findByAppId(appId)
                .orElseThrow(() -> new RuntimeException("Game with appId " + appId + " not found"));
    }

    public Game findById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game with id " + id + " not found"));
    }
}