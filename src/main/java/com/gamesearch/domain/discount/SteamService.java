package com.gamesearch.domain.discount;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamesearch.domain.coupon.Coupon;
import com.gamesearch.domain.coupon.CouponService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamService {
    private static final Logger logger = LoggerFactory.getLogger(SteamService.class);
    private static final String STEAM_DISCOUNT_URL = "https://store.steampowered.com/search/?specials=1&supportedlang=koreana&ndl=1";

    @Autowired
    private CouponService couponService;

    public List<Discount> getDiscountedGames(int page, int size) {
        List<Discount> games = fetchDiscountedGames(page, size);

        for (Discount game : games) {
            Long gameId = extractGameIdFromSteamUrl(game.getSteamUrl());
            List<Coupon> validCoupons = couponService.getValidCouponsForGame(gameId);

            for (Coupon coupon : validCoupons) {
                double newPrice = couponService.applyCoupon(game, coupon.getCode());
                if (newPrice < game.getFinalPrice()) {
                    game.setFinalPrice(newPrice);
                    game.addCoupon(coupon); // 수정된 부분: addCoupon 메서드 사용
                    break; // 가장 좋은 쿠폰 하나만 적용
                }
            }
        }

        return games;
    }

    private List<Discount> fetchDiscountedGames(int page, int size) {
        List<Discount> games = new ArrayList<>();
        String url = STEAM_DISCOUNT_URL + "&page=" + (page + 1);

        try {
            Document doc = Jsoup.connect(url).get();
            Elements gameElements = doc.select("#search_resultsRows > a");

            for (Element game : gameElements) {
                games.add(parseGameElement(game));
                if (games.size() >= size) {
                    break; // 원하는 수의 게임을 가져오면 종료
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

        return new Discount(name, discountPercentValue, originalPriceValue, discountedPriceValue, imageUrl, steamUrl);
    }

    private Long extractGameIdFromSteamUrl(String steamUrl) {
        // Steam URL에서 게임 ID 추출
        String[] parts = steamUrl.split("/");
        if (parts.length > 4) { // URL에서 게임 ID 추출
            try {
                return Long.parseLong(parts[4]); // 4번째 요소가 게임 ID임
            } catch (NumberFormatException e) {
                logger.warn("Failed to extract game ID from URL: {}", steamUrl);
            }
        }
        return null; // 유효하지 않은 경우 null 반환
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