package com.gamesearch.domain.coupon;

import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.discount.DiscountService; 
import com.gamesearch.domain.game.Game;
import com.gamesearch.domain.game.GameService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private GameService gameService; 

    @Autowired
    private DiscountService discountService; 

    // 쿠폰 생성 API
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(
            @RequestParam String code,
            @RequestParam float discountPercent,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate,
            @RequestParam Long gameId,
            @RequestParam String discountName) { // 할인 이름 추가

        // 게임 ID로 게임 객체 조회
        Game game = gameService.findById(gameId);
        if (game == null) {
            return ResponseEntity.badRequest().body(null); // 게임이 없으면 400 Bad Request 반환
        }

        // 할인 이름으로 할인 객체 조회
        Discount discount = discountService.findByName(discountName);
        if (discount == null) {
            return ResponseEntity.badRequest().body(null); // 할인이 없으면 400 Bad Request 반환
        }

        Coupon newCoupon = couponService.createCoupon(code, discountPercent, expirationDate, game, discount); 
        return ResponseEntity.ok(newCoupon);
    }

    // 유효한 쿠폰 조회 API
    @GetMapping("/valid")
    public ResponseEntity<List<Coupon>> getValidCoupons(@RequestParam Long gameId) { 
        List<Coupon> validCoupons = couponService.getValidCouponsForGame(gameId);
        return ResponseEntity.ok(validCoupons);
    }

    // 쿠폰 적용 API
    @PostMapping("/apply")
    public ResponseEntity<Double> applyCoupon(@RequestBody Discount discount,
                                              @RequestParam String couponCode) {
        double finalPrice = couponService.applyCoupon(discount, couponCode);
        return ResponseEntity.ok(finalPrice);
    }
}