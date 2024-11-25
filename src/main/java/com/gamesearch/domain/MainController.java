package com.gamesearch.domain;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping({"/","/index"}) // 루트 URL에 대한 요청을 처리
    public String index() {
        return "index"; // index.html 템플릿을 반환
    }
}
