package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.gamesearch.domain.user.User;
import com.gamesearch.domain.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserService userService;

    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    @Transactional
    public void addCommunity(Community community, String userEmail) {
        User currentUser = userService.findByEmail(userEmail);
        community.setAuthor(currentUser);
        communityRepository.save(community);
    }

    public List<Community> searchCommunities(String searchText) {
        return communityRepository.findByTitleContainingIgnoreCase(searchText);
    }

    public Community getCommunityById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public Community getCommunityByIdAndIncrementViewCount(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        community.setViewCount(community.getViewCount() + 1);
        return communityRepository.save(community);
    }

    public Page<Community> getAllCommunities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return communityRepository.findAll(pageable);
    }

    public Page<Community> getCommunityByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return communityRepository.findByCategory(category, pageable);
    }

    public Page<Community> searchCommunities(String searchText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return communityRepository.findByTitleContainingIgnoreCase(searchText, pageable);
    }

    public Page<Community> searchCommunities(String category, String searchText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if ("전체".equals(category)) {
            return communityRepository.findByTitleContainingIgnoreCase(searchText, pageable);
        } else {
            return communityRepository.findByCategoryAndTitleContainingIgnoreCase(category, searchText, pageable);
        }
    }

    @Transactional
    public void updateCommunity(Long id, Community updatedCommunity) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 수정 가능한 필드들만 업데이트
        community.setTitle(updatedCommunity.getTitle());
        community.setDescription(updatedCommunity.getDescription());
        community.setCategory(updatedCommunity.getCategory());

        // 변경된 엔티티는 자동으로 데이터베이스에 반영됩니다 (더티 체킹)
    }

    public void deleteCommunity(Long id) {
        communityRepository.deleteById(id);
    }

}
