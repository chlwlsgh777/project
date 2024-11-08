package com.gamesearch.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCommunityIdOrderByCreatedAtAsc(Long communityId);
}