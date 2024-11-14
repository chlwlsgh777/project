package com.gamesearch.domain.coupon;

import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.game.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private float discountPercent;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @ManyToOne // Game과의 관계 설정
    @JoinColumn(name = "game_id", nullable = false) // 외래 키 설정
    private Game game; // Game 객체로 변경

    public Coupon() {
        // 기본 생성자 필요 (JPA에서 사용)
    }

    public Coupon(String code, float discountPercent, LocalDate expirationDate, Game game) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
        this.game = game; // Game 객체로 변경
    }

    // Discount와의 관계를 추가할 경우 아래와 같이 추가할 수 있음
    @ManyToOne // Discount와의 관계 설정
    @JoinColumn(name = "discount_id") // 외래 키 설정
    private Discount discount; // Discount 객체로 변경
}