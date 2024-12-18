package com.gamesearch.domain.discount;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gamesearch.domain.game.Game;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class DiscountController {

    private final SteamService steamService;

    public DiscountController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/discount")
    public String discountPage(Model model) {
        List<Discount> initialGames = steamService.getDiscountedGames(0, 10);
        model.addAttribute("games", initialGames);
        model.addAttribute("hasMore", initialGames.size() >= 10);
        return "discount";
    }

    @GetMapping("/api/games")
    @ResponseBody
    public Map<String, Object> getGames(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Discount> games = steamService.getDiscountedGames(page, size);
        boolean hasMore = games.size() >= size;
        return Map.of("games", games, "hasMore", hasMore);
    }

    @GetMapping("/topselling")
    public String getTopSellingGames(Model model) {
        List<Game> games = steamService.getTopSellingGamesKR();
        model.addAttribute("games", games);
        return "index"; // Thymeleaf 템플릿 이름
    }
}