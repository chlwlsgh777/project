package com.gamesearch.domain.community;


public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getCommunity().getId(),
                comment.getAuthor().getNickname(),
                comment.getAuthor().getEmail(), // 이메일 매핑 추가
                comment.getCreatedAt()
        );
    }

    public static Comment toEntity(CommentDto dto) {
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setContent(dto.getContent());
        // community와 author는 별도로 설정해야 함
        return comment;
    }
}