package com.gamesearch.domain.admin;

import com.gamesearch.domain.user.UserService;
import com.gamesearch.domain.coupon.CouponService; // 쿠폰 서비스 추가
import com.gamesearch.domain.game.GameService; // 게임 서비스 추가

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CouponService couponService; // 쿠폰 서비스 주입
    private final GameService gameService; // 게임 서비스 주입

    public AdminController(UserService userService, CouponService couponService, GameService gameService) {
        this.userService = userService;
        this.couponService = couponService;
        this.gameService = gameService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 사용자 수, 게임 수, 쿠폰 수 등을 모델에 추가
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("gameCount", gameService.getGameCount());
        model.addAttribute("couponCount", couponService.getCouponCount());
        return "admin/dashboard"; // 대시보드 템플릿으로 이동
    }

    @PostMapping("/coupons/create")
    public String createCoupon(@RequestParam String code,
            @RequestParam float discountPercent,
            @RequestParam String expirationDate, // String으로 받기
            @RequestParam Long gameId) {
        // 문자열을 LocalDate로 변환
        LocalDate parsedExpirationDate = LocalDate.parse(expirationDate);

        // CouponService의 createCoupon 메서드 호출
        couponService.createCoupon(code, discountPercent, parsedExpirationDate, gameId);

        return "redirect:/admin/dashboard";
    }
}