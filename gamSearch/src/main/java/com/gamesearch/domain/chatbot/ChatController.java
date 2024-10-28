package com.gamesearch.domain.chatbot;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/")
    public String chat() {
        return "index";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getResponse(@RequestParam("userInput") String userInput) {
        logger.info("Received user input: {}", userInput);

        if (userInput == null || userInput.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "사용자 입력이 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String flaskResponse = callFlaskServer(userInput);
            logger.info("Response from Flask server: {}", flaskResponse);

            // Flask 서버로부터 받은 JSON 응답을 파싱
            Map<String, String> responseMap = objectMapper.readValue(flaskResponse, new TypeReference<Map<String, String>>() {});

            // \n을 <br>로 변환
            String formattedResponse = responseMap.get("response").replace("\n", "<br>");
            responseMap.put("response", formattedResponse);

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            logger.error("Error processing user input", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "요청 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private String callFlaskServer(String userInput) {
        String url = "http://localhost:5000/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("userInput", userInput);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }
}