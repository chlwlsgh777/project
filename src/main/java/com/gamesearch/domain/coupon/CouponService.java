package com.gamesearch.domain.coupon;

import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.game.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Transactional
    public Coupon createCoupon(String code, float discountPercent, LocalDate expirationDate, Game game, Discount discount) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountPercent(discountPercent);
        coupon.setExpirationDate(expirationDate);
        coupon.setGame(game); // 게임 객체 설정
    
        return couponRepository.save(coupon); // 쿠폰 저장
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public long getCouponCount() {
        return couponRepository.count();
    }

    public List<Coupon> getValidCouponsForGame(Long gameId) {
        LocalDate now = LocalDate.now();
        return couponRepository.findByGame_IdAndExpirationDateAfter(gameId, now);
    }

    // applyCoupon 메서드 추가
    public double applyCoupon(Discount discount, String couponCode) {
        // 쿠폰 코드로 쿠폰을 찾기
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        // 할인 적용 로직 (예: 최종 가격 계산)
        double finalPrice = discount.getFinalPrice() * (1 - (coupon.getDiscountPercent() / 100));

        return finalPrice; // 최종 가격 반환
    }
}