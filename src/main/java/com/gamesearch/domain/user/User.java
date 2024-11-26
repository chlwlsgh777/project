package com.gamesearch.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 12)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active; // Boolean 객체 타입 사용

    @PrePersist // 엔티티가 저장되기 전에 active 값을 강제로 true로 설정
    public void prePersist() {
        if (this.active == null) { // null이면 true로 설정
            this.active = true;
        }
    }

    public enum Role {
        USER, ADMIN
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isActive() {
        return this.active != null && this.active; // active가 true일 경우 true 반환
    }

}
