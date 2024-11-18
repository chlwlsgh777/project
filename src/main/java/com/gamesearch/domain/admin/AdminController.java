package com.gamesearch.domain.admin;

import com.gamesearch.domain.coupon.Coupon;
import com.gamesearch.domain.coupon.CouponService;
import com.gamesearch.domain.discount.Discount;
import com.gamesearch.domain.discount.DiscountService;
import com.gamesearch.domain.game.Game;
import com.gamesearch.domain.game.GameService;
import com.gamesearch.domain.user.User;
import com.gamesearch.domain.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private GameService gameService;

    @Autowired
    private DiscountService discountService; // DiscountService 주입 추가

    @Autowired
    private UserService userService; // UserService 주입 추가

    // 쿠폰 관리 페이지로 이동
    @GetMapping("/coupons")
    public String couponsManagement(Model model) {
        List<Coupon> coupons = couponService.getAllCoupons(); // 모든 쿠폰 조회
        List<Game> games = gameService.getAllGames(); // 모든 게임 조회
        model.addAttribute("coupons", coupons);
        model.addAttribute("games", games);
        return "admin/coupons"; // coupons.html 템플릿으로 이동
    }

    // 쿠폰 생성 API
    @PostMapping("/coupons/create")
    public String createCoupon(@RequestParam String code,
            @RequestParam float discountPercent,
            @RequestParam String expirationDate,
            @RequestParam Long appId,
            @RequestParam String discountName) { // 할인 이름 추가

        LocalDate parsedExpirationDate = LocalDate.parse(expirationDate);
        Game game = gameService.findByAppId(appId); // 게임 ID를 appID로 사용
        if (game == null) {
            throw new RuntimeException("Game not found"); // 예외 처리
        }

        Discount discount = discountService.findByName(discountName); // 할인 이름으로 할인 객체 조회
        if (discount == null) {
            throw new RuntimeException("Discount not found"); // 예외 처리
        }

        couponService.createCoupon(code, discountPercent, parsedExpirationDate, game, discount); // 쿠폰 생성
        return "redirect:/admin/coupons"; // 쿠폰 관리 페이지로 리다이렉트
    }

    // 대시보드 페이지로 이동
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("gameCount", gameService.getGameCount());
        model.addAttribute("couponCount", couponService.getCouponCount());
        return "admin/dashboard"; // 대시보드 템플릿으로 이동
    }

    // 사용자 목록 페이지로 이동
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers(); // 모든 사용자 조회
        model.addAttribute("users", users);
        return "admin/user_management"; // 사용자 관리 페이지로 이동
    }

    // 사용자 활성화 상태 업데이트 API
    @PostMapping("/users/{userId}/activate")
    public String updateUserActiveStatus(@PathVariable Long userId) {
        userService.toggleUserActiveStatus(userId); // 활성화 상태 전환
        return "redirect:/admin/users"; // 사용자 목록 페이지로 리다이렉트
    }
}