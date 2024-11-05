package com.gamesearch.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
    
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);
    
    // 이메일 또는 닉네임으로 사용자 찾기 (로그인 시 유용할 수 있음)
    Optional<User> findByEmailOrNickname(String email, String nickname);
}