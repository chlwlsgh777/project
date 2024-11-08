package com.gamesearch.domain.coupon;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Coupon {
    private String code;
    private float discountPercent;
    private LocalDate expirationDate;
    private String gameId;

    public Coupon(String code, float discountPercent, LocalDate expirationDate, String gameId) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
        this.gameId = gameId;
    }


}