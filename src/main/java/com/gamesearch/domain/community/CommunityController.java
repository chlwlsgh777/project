package com.gamesearch.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;
    private final CommentService commentService;

    @GetMapping
    public String communityPage(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "전체") String category) {
        int size = 5;
        Page<CommunityDto> communityPage;

        // 검색 또는 카테고리에 따라 커뮤니티 목록을 가져옵니다.
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

        // 페이지 네비게이션을 위한 시작 및 종료 페이지 설정
        int totalPages = communityPage.getTotalPages();
        int start = Math.max(0, page - 2);
        int end = Math.min(totalPages - 1, page + 2);

        if (end - start < 4 && totalPages > 5) {
            if (start == 0) {
                end = Math.min(4, totalPages - 1);
            } else if (end == totalPages - 1) {
                start = Math.max(0, totalPages - 5);
            }
        }

        model.addAttribute("startPage", start);
        model.addAttribute("endPage", end);

        return "community";
    }

    @GetMapping("/{id}")
    public String viewCommunity(@PathVariable Long id, Model model, Principal principal) {
        // 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
        if (principal == null) {
            return "redirect:/login?redirect=/community/" + id;
        }

        // 게시글 조회 및 조회 수 증가
        CommunityDto communityDto = communityService.getCommunityByIdAndIncrementViewCount(id);
        List<CommentDto> comments = commentService.getCommentsByCommunityId(id);

        model.addAttribute("communityDto", communityDto);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new CommentDto());

        // 현재 사용자 이름을 모델에 추가
        model.addAttribute("currentUsername", principal.getName());

        return "community-detail";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long id, Principal principal) {
        CommunityDto communityDto = communityService.getCommunityById(id);

        // 작성자 확인 후 삭제 수행
        if (communityDto.getAuthorEmail().equals(principal.getName())) {
            communityService.deleteCommunity(id);
            return ResponseEntity.ok().build(); // 성공적으로 삭제됨
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다."); // 권한 없음
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        CommunityDto communityDto = communityService.getCommunityById(id);

        // 작성자 확인 후 수정 폼 표시
        if (!communityDto.getAuthorEmail().equals(principal.getName())) {
            return "redirect:/community/" + id + "?error=unauthorized"; // 권한 없음
        }

        model.addAttribute("communityDto", communityDto);
        return "community-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCommunity(@PathVariable Long id, @ModelAttribute CommunityDto updatedCommunityDto,
            Principal principal) {

        CommunityDto existingCommunityDto = communityService.getCommunityById(id);

        // 작성자 확인 후 수정 수행
        if (!existingCommunityDto.getAuthorEmail().equals(principal.getName())) {
            return "redirect:/community/" + id + "?error=unauthorized"; // 권한 없음
        }

        updatedCommunityDto.setId(id);
        communityService.updateCommunity(updatedCommunityDto);

        return "redirect:/community/" + id; // 수정 후 게시글 상세 페이지로 리다이렉트
    }

    

    @GetMapping("/write")
    public String showWriteForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/community/write"; // 로그인 필요
        }
        model.addAttribute("communityDto", new CommunityDto());
        return "community-write"; // 글쓰기 폼 표시
    }

    @PostMapping("/write")
    public String createCommunity(@ModelAttribute CommunityDto communityDto, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/community/write"; // 로그인 필요
        }

        CommunityDto savedCommunity = communityService.createCommunity(communityDto, principal.getName());
        return "redirect:/community/" + savedCommunity.getId(); // 새 게시글 상세 페이지로 리다이렉트
    }
}