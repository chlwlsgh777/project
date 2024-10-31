package com.gamesearch.domain.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @GetMapping("/chatbot")
    public String chat() {
        return "chatbot";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<String> getResponse(@RequestParam("userInput") String userInput) {
        logger.info("Received user input: {}", userInput);

        if (userInput == null || userInput.isEmpty()) {
            return ResponseEntity.badRequest().body("사용자 입력이 없습니다.");
        }

        try {
            String response = callFlaskServer(userInput);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing user input", e);
            return ResponseEntity.internalServerError().body("요청 처리 중 오류가 발생했습니다.");
        }
    }

    private String callFlaskServer(String userInput) {
        String url = "http://localhost:5000/chat";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("userInput", userInput);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }
}