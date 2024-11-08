package com.gamesearch.domain.community;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String authorName; // 닉네임
    private String authorEmail; // 이메일 추가
    private Long authorId; // 작성자 ID 추가
    private LocalDateTime date;
    private int viewCount;
    private int commentCount; // 댓글 수 추가

    // 생성자, 메소드 등 필요한 경우 추가 가능
}