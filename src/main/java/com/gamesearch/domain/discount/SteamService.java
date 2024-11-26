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
import com.gamesearch.domain.game.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamService {
    private static final Logger logger = LoggerFactory.getLogger(SteamService.class);
    private static final String STEAM_DISCOUNT_URL = "https://store.steampowered.com/search/?specials=1&supportedlang=koreana&ndl=1";
    private static final String STEAM_TOP_SELLING_KR_URL = "https://store.steampowered.com/charts/topselling/KR";

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
                    game.addCoupon(coupon);
                    break;
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
                games.add(parseDiscountGameElement(game));
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

    private Discount parseDiscountGameElement(Element game) {
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

    public List<Game> getTopSellingGamesKR() {
        List<Game> games = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(STEAM_TOP_SELLING_KR_URL).get();
            Elements gameElements = doc.select(".weeklytopsellers_TableRow_3D6u5"); // 수정된 셀렉터

            for (Element gameElement : gameElements) {
                String name = gameElement.select(".weeklytopsellers_GameName_1n_4-").text(); // 게임 이름
                String priceString = gameElement.select(".weeklytopsellers_PriceCell_3v4Ys").text().replace("₩", "")
                        .replace(",", ""); // 가격
                String imageUrl = gameElement.select("img").attr("src"); // 이미지 URL
                String steamUrl = gameElement.select("a").attr("href"); // Steam URL

                // 가격 파싱 (무료인 경우 0으로 처리)
                Double price = priceString.isEmpty() || priceString.equalsIgnoreCase("무료")
                        ? 0.0
                        : parseDoubleSafely(priceString);

                // Game 객체로 변환
                Game game = new Game(name, price, imageUrl, steamUrl);
                games.add(game);
            }
            logger.info("Fetched {} top-selling games from Korea", games.size());
        } catch (IOException e) {
            logger.error("Error fetching top-selling games from Steam Korea", e);
        }
        return games;
    }

    private Long extractGameIdFromSteamUrl(String steamUrl) {
        String[] parts = steamUrl.split("/");
        if (parts.length > 4) {
            try {
                return Long.parseLong(parts[4]);
            } catch (NumberFormatException e) {
                logger.warn("Failed to extract game ID from URL: {}", steamUrl);
            }
        }
        return null;
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