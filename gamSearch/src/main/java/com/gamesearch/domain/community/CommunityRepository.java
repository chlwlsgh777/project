package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByCategory(String category);
}