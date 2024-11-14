package com.gamesearch.domain.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gamesearch.domain.discount.Discount;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // 쿠폰 생성 API
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestParam String code,
                                               @RequestParam float discountPercent,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate,
                                               @RequestParam Long gameId) { // 게임 ID 추가
        Coupon newCoupon = couponService.createCoupon(code, discountPercent, expirationDate, gameId);
        return ResponseEntity.ok(newCoupon);
    }

    // 유효한 쿠폰 조회 API
    @GetMapping("/valid")
    public ResponseEntity<List<Coupon>> getValidCoupons(@RequestParam Long gameId) { // 게임 ID로 필터링
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