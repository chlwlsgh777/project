package com.gamesearch.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Getter
@Setter
@ToString
public class Game {

    // 키 값
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게임 ID 번호
    @JsonProperty("app_id")
    @Column(nullable = false, unique = true)
    private Long appId;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    // 출시일
    @JsonProperty("release_date")
    @Column(nullable = false)
    private LocalDate releaseDate;

    // 가격
    @JsonProperty("price")
    @Column(nullable = false)
    private double price;

    // 추천수
    @JsonProperty("recommendations")
    private int recommendations;

    // 지원 언어
    @JsonProperty("supported_languages")
    @Column(length = 500)
    private List<String> supportedLanguages;

    // 카테고리
    @JsonProperty("categories")
    @ElementCollection
    private List<String> categories;

    // 장르
    @JsonProperty("genres")
    @Column(name = "genre")
    @ElementCollection
    private List<String> genre;

    // 긍정적 리뷰 개수
    @JsonProperty("positive")
    private int positive;

    // 부정적 리뷰 개수
    @JsonProperty("negative")
    private int negative;

    // 긍정적 비율
    @JsonProperty("positive_rate")
    private int positiveRate;

    // 평가
    @JsonProperty("evaluation")
    private String evaluation;

    // 태그
    @JsonProperty("tags")
    @Column(name = "tag")
    @ElementCollection
    private List<String> tag;

    // 기본생성자
    public Game() {

    }

}
