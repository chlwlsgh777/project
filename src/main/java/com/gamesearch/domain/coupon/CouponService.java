package com.gamesearch.domain.coupon;

import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.game.Game;
import com.gamesearch.domain.game.GameService;
import com.gamesearch.domain.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private GameService gameService;

    @Transactional
    public Coupon createCoupon(String code, float discountPercent, LocalDate expirationDate, Game game,
            Discount discount) {
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

    @Transactional
    public Coupon issueCoupon(Long gameId, User user) {
        // 이미 발급된 쿠폰이 있는지 확인
        if (couponRepository.existsByGameIdAndUser(gameId, user)) {
            throw new RuntimeException("이미 이 게임의 쿠폰을 발급받으셨습니다.");
        }

        Game game = gameService.findById(gameId);
        if (game == null) {
            throw new RuntimeException("해당 게임을 찾을 수 없습니다.");
        }

        // 쿠폰 생성 로직
        String couponCode = generateCouponCode();
        LocalDate expirationDate = LocalDate.now().plusDays(30); // 30일 후 만료
        float discountPercent = 10; // 10% 할인

        Coupon coupon = new Coupon(couponCode, discountPercent, expirationDate, game);
        coupon.setUser(user);

        return couponRepository.save(coupon);
    }

    private String generateCouponCode() {
        return "COUPON-" + UUID.randomUUID().toString().substring(0, 8);
    }

}