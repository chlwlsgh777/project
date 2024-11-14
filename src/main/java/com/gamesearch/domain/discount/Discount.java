package com.gamesearch.domain.discount;

import com.gamesearch.domain.coupon.Coupon;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int discountPercent;
    private double originalPrice;
    private double finalPrice;
    private String imageUrl;
    private String steamUrl;

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Coupon> coupons = new ArrayList<>(); // 초기화

    public Discount(String name, int discountPercent, double originalPrice, double finalPrice, String imageUrl,
                    String steamUrl) {
        this.name = name;
        this.discountPercent = discountPercent;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.imageUrl = imageUrl;
        this.steamUrl = steamUrl;
    }

    // 기본 생성자 필요 (JPA에서 사용)
    public Discount() {
    }

    // 쿠폰 추가 메서드
    public void addCoupon(Coupon coupon) {
        coupons.add(coupon);
        coupon.setDiscount(this); // 양방향 관계 설정
    }
}