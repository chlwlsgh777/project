package com.gamesearch.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (개발 환경에서만 사용 권장)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/chat", "/index").permitAll() // 특정 경로에 대한 접근 허용
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}

