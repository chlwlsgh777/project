package com.gamesearch.domain.user;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Transactional
    public User registerUser(User user) {
        validateEmailAndNickname(user.getEmail(), user.getNickname());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    private void validateEmailAndNickname(String email, String nickname) {
        if (!isValidEmail(email)) {
            throw new RuntimeException("유효하지 않은 이메일 형식입니다.");
        }
        if (!isEmailAvailable(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (!isNicknameAvailable(nickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isEmailAvailable(String email) {
        return isValidEmail(email) && !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public String getNicknameByEmail(String email) {
        User user = findByEmail(email);
        return user.getNickname();
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
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email or nickname: " + emailOrNickname));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void changeUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public boolean isAdmin(String email) {
        User user = findByEmail(email);
        return user.isAdmin();
    }

    public long getUserCount() {
        return userRepository.count(); // 예시: JPA Repository 사용
    }
}
