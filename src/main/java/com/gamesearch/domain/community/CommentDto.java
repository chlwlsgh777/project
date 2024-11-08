package com.gamesearch.domain.community;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private Long communityId;
    private String authorName;
    private String authorEmail; // 작성자 이메일 추가
    private LocalDateTime createdAt;
}
