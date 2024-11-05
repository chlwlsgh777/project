package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByCategory(String category);
    List<Community> findByTitleContainingIgnoreCase(String title);
    Page<Community> findAll(Pageable pageable);
    Page<Community> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Community> findByCategory(String category, Pageable pageable);
    Page<Community> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Pageable pageable);
   
}