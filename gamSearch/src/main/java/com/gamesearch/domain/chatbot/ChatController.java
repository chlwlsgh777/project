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

    @GetMapping("/chatbot")
    public String chat() {
        return "chatbot";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResponse(@RequestParam("userInput") String userInput) {
        logger.info("Received user input: {}", userInput);

        if (userInput == null || userInput.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("response", "사용자 입력이 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Flask 서버로부터 응답 받기
            String flaskResponse = callFlaskServer(userInput);
            logger.info("Response from Flask server: {}", flaskResponse);

            // Flask 응답 파싱 (이중 JSON 처리 가능)
            Map<String, Object> responseMap = parseFlaskResponse(flaskResponse);

            // \n -> <br> 변환 처리
            if (responseMap.containsKey("response")) {
                String formattedResponse = responseMap.get("response").toString().replace("\n", "<br>");
                responseMap.put("response", formattedResponse);
            }

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            logger.error("Error processing user input", e);
            Map<String, Object> errorResponse = new HashMap<>();
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

    /**
     * Flask 서버로부터 받은 응답을 파싱하는 메서드
     * 이중 JSON 문자열도 처리 가능
     */
    private Map<String, Object> parseFlaskResponse(String flaskResponse) throws Exception {
        try {
            // 첫 번째 파싱 시도: JSON 객체로 파싱
            return objectMapper.readValue(flaskResponse, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("First parsing attempt failed. Retrying with nested JSON...");
            
            // 두 번째 시도: 응답이 이중으로 감싸진 경우 파싱
            Map<String, String> tempMap = objectMapper.readValue(flaskResponse, new TypeReference<Map<String, String>>() {});
            String nestedJson = tempMap.get("response");

            // 이중으로 감싸진 JSON 문자열을 다시 파싱
            return objectMapper.readValue(nestedJson, new TypeReference<Map<String, Object>>() {});
        }
    }
}
