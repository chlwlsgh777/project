package com.gamesearch.domain.discount;

import com.gamesearch.domain.coupon.Coupon;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Discount {
    private String name;
    private int discountPercent;
    private double originalPrice;
    private double finalPrice;
    private String imageUrl;
    private String steamUrl;
    private Coupon coupon;

    public Discount(String name, int discountPercent, double originalPrice, double finalPrice, String imageUrl, String steamUrl) {
        this.name = name;
        this.discountPercent = discountPercent;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.imageUrl = imageUrl;
        this.steamUrl = steamUrl;
    }

}