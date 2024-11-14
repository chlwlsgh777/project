package com.gamesearch.domain.community;

import com.gamesearch.domain.user.User;
import com.gamesearch.domain.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserService userService;
    private final CommentService commentService;

    
    public Page<CommunityDto> getAllCommunities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Community> communityPage = communityRepository.findAll(pageable);
        return communityPage.map(CommunityMapper::toDto);
    }

    @Transactional
    public CommunityDto createCommunity(CommunityDto communityDto, String userEmail) {
        User currentUser = userService.findByEmail(userEmail);
        Community community = new Community();
        community.setTitle(communityDto.getTitle());
        community.setDescription(communityDto.getDescription());
        community.setCategory(communityDto.getCategory());
        community.setAuthor(currentUser);
        community.setViewCount(0);
        Community savedCommunity = communityRepository.save(community);
        return CommunityMapper.toDto(savedCommunity);
    }

    public Page<CommunityDto> searchCommunities(String category, String searchText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Community> communityPage;
        if ("전체".equals(category)) {
            communityPage = communityRepository.findByTitleContainingIgnoreCase(searchText, pageable);
        } else {
            communityPage = communityRepository.findByCategoryAndTitleContainingIgnoreCase(category, searchText,
                    pageable);
        }
        return communityPage.map(CommunityMapper::toDto);
    }

    public CommunityDto getCommunityById(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return CommunityMapper.toDto(community);
    }

    
    @Transactional
    public CommunityDto getCommunityByIdAndIncrementViewCount(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);
        return CommunityMapper.toDto(community);
    }

    public Page<CommunityDto> getCommunityByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Community> communityPage = communityRepository.findByCategory(category, pageable);
        return communityPage.map(CommunityMapper::toDto);
    }

    @Transactional
    public void updateCommunity(CommunityDto updatedCommunityDto) {
        Community community = communityRepository.findById(updatedCommunityDto.getId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        community.setTitle(updatedCommunityDto.getTitle());
        community.setDescription(updatedCommunityDto.getDescription());
        community.setCategory(updatedCommunityDto.getCategory());

        communityRepository.save(community);
    }

    @Transactional
    public void deleteCommunity(Long id) {
        List<Comment> comments = commentService.getCommentEntitiesByCommunityId(id);
        for (Comment comment : comments) {
            commentService.deleteComment(comment.getId());
        }

        communityRepository.deleteById(id);
    }

    // 마이페이지에서 작성한 게시글 아이디를 통해서 찾기위한 함수.
    public List<Community> findByAuthor(User author) {
        return communityRepository.findByAuthor(author);
    }


    
}