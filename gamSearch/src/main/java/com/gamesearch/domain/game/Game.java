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
    @Column(nullable = false, name = "app_id", unique = true)
    private Long appId;

    @JsonProperty("name")
    @Column(nullable = false, name = "name")
    private String name;


    // 출시일
    @JsonProperty("release_date")
    @Column(nullable = false, name = "release_date")
    private LocalDate releaseDate;

    // 가격
    @JsonProperty("price")
    @Column(nullable = false, name = "price")
    private double price;

    // 추천수
    @JsonProperty("recommendations")
    @Column(name = "recommendations")
    private int recommendations;

    // 지원 언어
    @JsonProperty("supported_languages")
    @ElementCollection
    private List<String> supportedLanguages;

    // 카테고리
    @JsonProperty("categories")
    @ElementCollection
    private List<String> categories;

    // 장르
    @JsonProperty("genres")
    @ElementCollection
    private List<String> genres;

    // 긍정적 리뷰 개수
    @JsonProperty("positive")
    @Column(name = "positive")
    private int positive;

    // 부정적 리뷰 개수
    @JsonProperty("negative")
    @Column(name = "negative")
    private int negative;

    // 긍정적 비율
    @JsonProperty("positive_rate")
    @Column(name = "positive_rate")
    private int positiveRate;

    // 평가
    @JsonProperty("evaluation")
    @Column(name = "evaluation")
    private String evaluation;


    // 태그
    @JsonProperty("tags")
    @ElementCollection
    private List<String> tags;


    
    // 기본생성자
    public Game() {

    }







    


    
}
