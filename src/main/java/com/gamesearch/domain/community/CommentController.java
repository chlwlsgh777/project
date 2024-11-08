package com.gamesearch.domain.community;

import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gamesearch.domain.user.UserService;

@RestController
@RequestMapping("/community")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    // 댓글 추가
    @PostMapping(value = "/{id}/comment", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long id,
            @RequestBody CommentDto commentDto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인 필요
        }

        String userEmail = principal.getName();
        CommentDto savedComment = commentService.addComment(id, commentDto.getContent(), userEmail);
        return ResponseEntity.ok(savedComment);
    }

    // 댓글 수정
    @PutMapping(value = "/comment/{commentId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long commentId,
            @RequestBody CommentDto commentDto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인 필요
        }

        CommentDto existingComment = commentService.getCommentById(commentId);
        if (existingComment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 댓글이 없을 때
        }

        String currentUserNickname = userService.getNicknameByEmail(principal.getName());
        if (!existingComment.getAuthorName().equals(currentUserNickname)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한이 없는 경우
        }

        CommentDto updatedComment = commentService.updateComment(commentId, commentDto.getContent());
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 로그인 필요
        }

        CommentDto existingComment = commentService.getCommentById(commentId);
        if (existingComment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String currentUserNickname = userService.getNicknameByEmail(principal.getName());
        if (!existingComment.getAuthorName().equals(currentUserNickname)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
