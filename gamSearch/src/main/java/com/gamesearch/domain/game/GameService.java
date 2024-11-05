package com.gamesearch.domain.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;



@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        List<Game> games = objectMapper.readValue(new File("src/main/resources/games/extracted_games_data.json"), new TypeReference<List<Game>>() {});

        // 데이터베이스에 저장
        for (Game game : games) {
            if (!gameRepository.existsByAppId(game.getAppId())) {  // 중복 체크
                gameRepository.save(game);
            }
        }
    
    }
}


