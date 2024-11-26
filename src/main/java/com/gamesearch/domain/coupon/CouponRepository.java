package com.gamesearch.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCode(String code);
    List<Coupon> findByGame_IdAndExpirationDateAfter(Long gameId, LocalDate date);
    Optional<Coupon> findByCode(String code);
    boolean existsByGame_IdAndUser_Id(Long gameId, Long userId);
}