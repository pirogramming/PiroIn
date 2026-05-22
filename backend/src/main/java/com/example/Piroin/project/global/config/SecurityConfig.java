package com.example.Piroin.project.global.config;

import com.example.Piroin.project.global.jwt.JwtAuthenticationEntryPoint;
import com.example.Piroin.project.global.jwt.JwtAuthenticationFilter;
import com.example.Piroin.project.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 로그인 페이지는 로그인 안 된 상태에서 접근 가능
                        .requestMatchers("/api/auth/login").permitAll()

                        // curriculum: GET은 로그인한 누구나, POST/PATCH/DELETE는 ADMIN만 -> 이중 보안 느낌
                        .requestMatchers(HttpMethod.GET, "/api/curriculums").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/curriculums").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/curriculums/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/curriculums/{sessionDate}").hasRole("ADMIN")

                        // understanding check: 생성은 ADMIN만 가능
                        .requestMatchers(HttpMethod.POST, "/api/sessions/{sessionId}/understanding-checks").hasRole("ADMIN")

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Actuator health check
                        .requestMatchers("/actuator/health").permitAll()

                        // 다른 도메인 권한 설정 필요 시 위 패턴 참고해서 추가
                        // 단, 추가하지 않아도 무방함
                        // 이유 1. anyRequest().authenticated()로 비로그인 접근 차단
                        // 이유 2. 프론트에서 ADMIN 전용 버튼/기능을 UI 단에서 숨김 처리
                        .anyRequest().authenticated()

                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }
}
