package com.gamesearch.domain.discount;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamService {
    private static final Logger logger = LoggerFactory.getLogger(SteamService.class);
    private static final String STEAM_DISCOUNT_URL = "https://store.steampowered.com/search/?specials=1&supportedlang=koreana&ndl=1";

    public List<Discount> getDiscountedGames(int page, int size) {
        List<Discount> games = new ArrayList<>();
        String url = STEAM_DISCOUNT_URL + "&page=" + (page + 1); // Steam uses 1-based indexing

        try {
            Document doc = Jsoup.connect(url).get();
            Elements gameElements = doc.select("#search_resultsRows > a");

            for (Element game : gameElements) {
                games.add(parseGameElement(game));
                if (games.size() >= size) {
                    break;
                }
            }

            logger.info("Fetched {} games from page {}", games.size(), page + 1);
        } catch (IOException e) {
            logger.error("Error fetching discounted games from Steam on page {}", page + 1, e);
        }

        return games;
    }

    private Discount parseGameElement(Element game) {
        String name = game.select(".title").text();
        String discountPercent = game.select(".discount_pct").text().replace("-", "").replace("%", "");
        String originalPrice = game.select(".discount_original_price").text().replace("₩", "").replace(",", "");
        String discountedPrice = game.select(".discount_final_price").text().replaceAll(".*₩", "").replace(",", "");

        int discountPercentValue = parseIntSafely(discountPercent);
        double originalPriceValue = parseDoubleSafely(originalPrice);
        double discountedPriceValue = parseDoubleSafely(discountedPrice);

        String imageUrl = game.select("img").attr("src");
        String steamUrl = game.attr("href");

        logger.debug("Game: {}, Discount: {}%, Original: {}, Discounted: {}", 
            name, discountPercentValue, originalPriceValue, discountedPriceValue);

        return new Discount(
            name,
            discountPercentValue,
            originalPriceValue,
            discountedPriceValue,
            imageUrl,
            steamUrl
        );
    }

    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse int value: {}", value);
            return 0;
        }
    }

    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse double value: {}", value);
            return 0.0;
        }
    }
}