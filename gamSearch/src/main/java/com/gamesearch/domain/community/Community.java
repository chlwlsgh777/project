package com.gamesearch.domain.community;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String category; // 카테고리 이름을 소문자로 변경
    private String title;
    private String name;
    private String description;
    private String date;

    // 추가적인 필드나 메서드가 필요할 경우 여기에 추가
    public void printCommunityTitle(Community community) {
        // getTitle() 메소드 호출
        String communityTitle = community.getTitle();
        System.out.println("Community Title: " + title);
    }
}