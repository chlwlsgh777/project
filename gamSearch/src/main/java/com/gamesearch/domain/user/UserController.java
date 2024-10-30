package com.gamesearch.domain.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login?registered";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "이미 존재하는 이메일입니다.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        boolean isAvailable = userService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
public String loginUser(@RequestParam String email, @RequestParam String password, 
                        Model model) {
    try {
        // Spring Security의 AuthenticationManager를 통해 인증을 처리하도록 변경
        userService.loginUser(email, password);
        return "redirect:/index"; // 로그인 성공 시 인덱스로 리다이렉트
    } catch (RuntimeException e) {
        model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return "login"; // 실패 시 로그인 페이지로 돌아감
    }
}
    @GetMapping("/mypage")
    public String showMyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    if (userDetails == null) {
        return "redirect:/login"; // 인증되지 않은 경우 로그인 페이지로 리다이렉트
    }
    User user = userService.findByEmail(userDetails.getUsername());
    model.addAttribute("user", user);
    return "mypage"; // 마이페이지 템플릿 반환
}

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index?logout";
    }
}