package com.gamesearch.domain.community;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.gamesearch.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String category;
    private String title;

    @Column(columnDefinition = "TEXT") // 본문을 TEXT 타입으로 설정
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @CreationTimestamp
    private LocalDateTime date;

    private int viewCount = 0;

    private int commentCount = 0;
}