package com.gamesearch.domain.user;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.gamesearch.domain.community.Community;
import com.gamesearch.domain.community.CommunityService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommunityService communityService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@ModelAttribute("user") User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.registerUser(user);
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String redirect, Model model) {
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        return "login";
    }

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String userEmail = userDetails.getUsername();
        User user = userService.findByEmail(userEmail);
        List<Community> userPosts = communityService.findByAuthor(user);

        model.addAttribute("user", user);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("isAdmin", userService.isAdmin(user.getEmail()));
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
    public String logout() {
        return "redirect:/login?logout";
    }

    @GetMapping("/api/check-login")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkLogin(Principal principal) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isLoggedIn", principal != null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Map<String, Object> response = new HashMap<>();

        if (!userService.isValidEmail(email)) {
            response.put("available", false);
            response.put("message", "유효하지 않은 이메일 형식입니다.");
        } else if (userService.isEmailAvailable(email)) {
            response.put("available", true);
            response.put("message", "사용 가능한 이메일입니다.");
        } else {
            response.put("available", false);
            response.put("message", "이미 사용 중인 이메일입니다.");
        }

        return response;
    }

    @PostMapping("/check-nickname")
    @ResponseBody
    public Map<String, Boolean> checkNickname(@RequestBody Map<String, String> payload) {
        String nickname = payload.get("nickname");
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return Collections.singletonMap("available", isAvailable);
    }
}