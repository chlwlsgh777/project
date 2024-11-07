package com.gamesearch.domain.community;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping
    public String communityPage(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "전체") String category) {
        int size = 20; // 페이지당 게시물 수
        Page<Community> communityPage;

        if (search != null && !search.isEmpty()) {
            communityPage = communityService.searchCommunities(category, search, page, size);
        } else if (!category.equals("전체")) {
            communityPage = communityService.getCommunityByCategory(category, page, size);
        } else {
            communityPage = communityService.getAllCommunities(page, size);
        }

        model.addAttribute("communities", communityPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", communityPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        return "community";
    }

    @GetMapping("/write")
    public String showWriteForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/community/write";
        }
        model.addAttribute("community", new Community());
        return "community-write";
    }

    @PostMapping("/write")
    public String writePost(@ModelAttribute Community community, Principal principal) {
        communityService.addCommunity(community, principal.getName());
        return "redirect:/community";
    }

    @GetMapping("/{id}")
    public String viewCommunity(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/community/" + id;
        }
        Community community = communityService.getCommunityById(id);
        model.addAttribute("community", community);
        if (principal != null) {
            model.addAttribute("currentUserEmail", principal.getName());
        }
        return "community-detail";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long id, Principal principal) {
        Community community = communityService.getCommunityById(id);
        if (community.getAuthor().getEmail().equals(principal.getName())) {
            communityService.deleteCommunity(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Community community = communityService.getCommunityById(id);
        if (!community.getAuthor().getEmail().equals(principal.getName())) {
            return "redirect:/community/" + id + "?error=unauthorized";
        }
        model.addAttribute("community", community);
        return "community-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCommunity(@PathVariable Long id, @ModelAttribute Community updatedCommunity,
            Principal principal) {
        Community community = communityService.getCommunityById(id);
        if (!community.getAuthor().getEmail().equals(principal.getName())) {
            return "redirect:/community/" + id + "?error=unauthorized";
        }
        communityService.updateCommunity(id, updatedCommunity);
        return "redirect:/community/" + id;
    }

}