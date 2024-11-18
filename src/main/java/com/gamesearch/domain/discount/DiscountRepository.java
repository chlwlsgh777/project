package com.gamesearch.domain.discount;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // 할인 이름으로 할인 정보를 조회하는 메서드
    Discount findByName(String name);
}