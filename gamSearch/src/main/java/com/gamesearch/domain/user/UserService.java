package com.gamesearch.domain.user;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        if (!isEmailAvailable(user.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (!isNicknameAvailable(user.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateNickname(User user, String newNickname) {
        if (!isNicknameAvailable(newNickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        user.setNickname(newNickname);
        return userRepository.save(user);
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with nickname: " + nickname));
    }

    public User findByEmailOrNickname(String emailOrNickname) {
        return userRepository.findByEmailOrNickname(emailOrNickname, emailOrNickname)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or nickname: " + emailOrNickname));
    }
}