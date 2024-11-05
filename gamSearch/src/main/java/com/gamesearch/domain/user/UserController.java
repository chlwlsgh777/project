package com.gamesearch.domain.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

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
            model.addAttribute("error", "이미 존재하는 이메일 또는 닉네임입니다.");
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

    @PostMapping("/check-nickname")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestBody Map<String, String> payload) {
        String nickname = payload.get("nickname");
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        return "login";
    }

    @GetMapping("/mypage")
    public String showMyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "mypage";
    }

    @PostMapping("/update-nickname")
    public String updateNickname(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String newNickname, Model model) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.updateNickname(user, newNickname);
            model.addAttribute("success", "닉네임이 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            model.addAttribute("error", "닉네임 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/mypage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index?logout";
    }

    @GetMapping("/api/check-login")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkLogin(Principal principal) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isLoggedIn", principal != null);
        return ResponseEntity.ok(response);
    }
}