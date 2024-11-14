package com.gamesearch.domain.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamesearch.domain.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByCategory(String category);
    List<Community> findByTitleContainingIgnoreCase(String title);
    List<Community> findByAuthor(User author);
    
    Page<Community> findByCategory(String category, Pageable pageable);
   

    
    Page<Community> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
    Page<Community> findByAuthor_NicknameContainingIgnoreCase(String authorName, Pageable pageable);
    
    Page<Community> findByCategoryAndDescriptionContainingIgnoreCase(String category, String description, Pageable pageable);
    Page<Community> findByCategoryAndAuthor_NicknameContainingIgnoreCase(String category, String authorName, Pageable pageable);


    Page<Community> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Community> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Pageable pageable);

    @Query("SELECT c FROM Community c WHERE " +
           "(:category = '전체' OR c.category = :category) AND " +
           "(:searchText IS NULL OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.author.nickname) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Community> searchAllFields(
        @Param("category") String category,
        @Param("searchText") String searchText,
        Pageable pageable
    );
   
}