package com.gamesearch.domain.community;

import java.security.Principal;

import org.springframework.data.domain.Page;
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
    public String viewCommunity(@PathVariable Long id, Model model) {
        Community community = communityService.getCommunityByIdAndIncrementViewCount(id);
        model.addAttribute("community", community);
        return "community-detail";
    }

}