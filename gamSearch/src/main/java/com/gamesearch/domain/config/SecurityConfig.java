package com.gamesearch.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gamesearch.domain.user.CustomUserDetailsService;
import com.gamesearch.domain.user.CustomUserDetailsService.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/check-email", "/check-nickname", "/chat", "/api/check-login"))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/chat", "/index", "/register", "/login", "/check-email", "/chatbot",
                                "/community", "/check-nickname", "/api/check-login", "/community/**")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
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
            if (authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserDetails userDetails) {
                
                // 만약 사용자의 실제 이름을 저장하고 싶다면, CustomUserDetails에 getName() 메서드를 추가하고 아래와 같이 사용
                session.setAttribute("userName", ((CustomUserDetails)
                userDetails).getName());
            }

            String redirectUrl = request.getParameter("redirect");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:9090")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}