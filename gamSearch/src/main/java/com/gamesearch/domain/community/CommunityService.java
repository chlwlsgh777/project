package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class CommunityService {
    
    @Autowired
    private CommunityRepository communityRepository;

    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    public void addCommunity(Community community) {
        communityRepository.save(community);
    }

    public List<Community> searchCommunities(String searchText) {
        return communityRepository.findAll().stream()
            .filter(community -> community.getTitle().toLowerCase().contains(searchText.toLowerCase()))
            .toList();
    }
}
