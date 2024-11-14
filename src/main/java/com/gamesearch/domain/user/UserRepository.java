package com.gamesearch.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByEmail(String email);
    
    Optional<User> findByNickname(String nickname);
    
    boolean existsByNickname(String nickname);
    
    Optional<User> findByEmailOrNickname(String email, String nickname);

    Optional<User> findByEmail(String email);
}