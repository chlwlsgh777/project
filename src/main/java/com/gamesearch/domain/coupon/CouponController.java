package com.gamesearch.domain.coupon;

import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.discount.DiscountService; 
import com.gamesearch.domain.game.Game;
import com.gamesearch.domain.game.GameService;
import com.gamesearch.domain.user.User;
import com.gamesearch.domain.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private GameService gameService; 

    @Autowired
    private DiscountService discountService; 

    @Autowired
    private UserService userService; 

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

    @PostMapping("/issue-coupon")
    public ResponseEntity<?> issueCoupon(@RequestBody Map<String, Object> payload) {
        Long gameId = Long.parseLong(payload.get("gameId").toString());
        String userEmail = payload.get("userEmail").toString(); // 사용자 이메일로 변경
        
        try {
            User user = userService.findByEmail(userEmail);
            if (user == null || !user.isActive()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 사용자입니다."));
            }

            Coupon coupon = couponService.issueCoupon(gameId, user);
            return ResponseEntity.ok(Map.of("success", true, "couponCode", coupon.getCode()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
