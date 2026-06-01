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
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CorsConfig에서 등록한 CORS 설정을 Spring Security 필터 체인에 적용
                // 이 설정이 없으면 preflight(OPTIONS) 요청이 Security 단에서 차단되어 405 반환
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 로그인
                        .requestMatchers("/api/auth/login").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Actuator health check
                        .requestMatchers("/actuator/health").permitAll()

                        // ADMIN 전용 엔드포인트
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/curriculums").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/curriculums/{sessionDate}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/curriculums/{sessionDate}").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/assignments/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/assignments/modify/{assignmentId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/{assignmentId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/assignments/{week}/view").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/deposit/{userId}/deposit/view").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/deposit/{userId}/deposit/defence").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/sessions/{sessionId}/understanding-checks").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/questions/{questionId}/status").hasRole("ADMIN")

                        // 나머지는 로그인한 사용자면 접근 가능
                        .anyRequest().authenticated()

                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }
}
