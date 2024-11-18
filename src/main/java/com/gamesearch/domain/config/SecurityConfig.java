// SecurityConfig.java
package com.gamesearch.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gamesearch.domain.user.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/login", "/check-email", "/check-nickname", "/chat",
                                "/api/check-login",
                                "/api/games"))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/chat", "/index", "/register", "/login",
                                "/check-email", "/chatbot", "/community",
                                "/check-nickname", "/api/check-login",
                                "/discount", "/community/**", "/api/games") // API 요청 허용
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 페이지 접근 제한
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied"))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/login?expired"))
                .sessionManagement(session -> session
                        .sessionFixation().newSession());
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession();
            if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                session.setAttribute("userName", userDetails.getName());
                session.setAttribute("userRole", userDetails.getAuthorities().toString());
            }

            String redirectUrl = request.getParameter("redirect");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect("/");
            }
            logger.info("Authentication successful for user: " + authentication.getName());
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String errorMessage = exception.getMessage().contains("disabled")
                    ? "관리자에 의해 정지된 계정입니다."
                    : "이메일 또는 비밀번호가 올바르지 않습니다.";

            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            logger.info("Authentication failed: " + exception.getMessage());

            response.sendRedirect("/login?error=true&errorMessage=" + encodedMessage);
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:9090") // 클라이언트 URL에 맞게 수정 필요
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
