package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/community")
public class CommunityController {
    @Autowired
    private CommunityService communityService;

    @GetMapping
    public String communityPage(Model model, @RequestParam(required = false) String search) {
        List<Community> communities = (search != null && !search.isEmpty()) ? 
            communityService.searchCommunities(search) : 
            communityService.getAllCommunities();
        model.addAttribute("communities", communities);
        return "community"; // community.html로 이동
    }

    @PostMapping("/add")
    public String addCommunity(@ModelAttribute Community community) {
        communityService.addCommunity(community);
        return "redirect:/community"; // 게시물 추가 후 커뮤니티 페이지로 리다이렉트
    }
}
