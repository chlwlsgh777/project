package com.gamesearch.domain.community;

import com.gamesearch.domain.user.User;
import com.gamesearch.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    // 특정 커뮤니티에 대한 모든 댓글 가져오기
    public List<CommentDto> getCommentsByCommunityId(Long communityId) {
        List<Comment> comments = commentRepository.findByCommunityIdOrderByCreatedAtAsc(communityId);
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    // 댓글 추가
    @Transactional
    public CommentDto addComment(Long communityId, String content, String userEmail) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userService.findByEmail(userEmail);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCommunity(community);
        comment.setAuthor(user);

        Comment savedComment = commentRepository.save(comment);
        CommentDto commentDto = CommentMapper.toDto(savedComment);
        commentDto.setAuthorName(user.getNickname());
        return commentDto;
    }

    // 댓글 수정
    @Transactional
    public CommentDto updateComment(Long commentId, String newContent) {
        Comment comment = getCommentEntityById(commentId);
        comment.setContent(newContent);
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toDto(updatedComment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = getCommentEntityById(commentId);
        commentRepository.delete(comment);
    }

    // 특정 댓글 ID로 댓글 가져오기 (DTO 반환)
    public CommentDto getCommentById(Long commentId) {
        Comment comment = getCommentEntityById(commentId);
        return CommentMapper.toDto(comment);
    }

    // 특정 댓글 ID로 댓글 엔티티 가져오기 (내부 사용)
    private Comment getCommentEntityById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
    }

    public List<Comment> getCommentEntitiesByCommunityId(Long communityId) {
        return commentRepository.findByCommunityIdOrderByCreatedAtAsc(communityId);
    }
}