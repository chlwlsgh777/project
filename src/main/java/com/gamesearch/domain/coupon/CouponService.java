package com.gamesearch.domain.coupon;

import org.springframework.stereotype.Service;

import com.gamesearch.domain.discount.Discount;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    private List<Coupon> coupons = new ArrayList<>();

    // 특정 게임에 대한 쿠폰 생성
    public Coupon createCoupon(String code, float discountPercent, LocalDate expirationDate, String gameId) {
        Coupon newCoupon = new Coupon(code, discountPercent, expirationDate, gameId);
        coupons.add(newCoupon);
        return newCoupon;
    }

    // 특정 게임에 대한 유효한 쿠폰 조회
    public List<Coupon> getValidCouponsForGame(String gameId) {
        LocalDate currentDate = LocalDate.now();
        return coupons.stream()
                .filter(coupon -> coupon.getExpirationDate().isAfter(currentDate) && coupon.getGameId().equals(gameId))
                .collect(Collectors.toList());
    }

    // 쿠폰 적용
    public double applyCoupon(Discount discount, String couponCode) {
        return coupons.stream()
                .filter(coupon -> coupon.getCode().equals(couponCode))
                .findFirst()
                .map(coupon -> discount.getFinalPrice() * (1 - coupon.getDiscountPercent() / 100))
                .orElse(discount.getFinalPrice());
    }
}