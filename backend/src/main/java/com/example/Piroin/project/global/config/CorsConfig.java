package com.example.Piroin.project.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 모든 출처(도메인) 허용 - Vercel 프론트엔드 등 다양한 도메인에서 요청 가능
        config.setAllowedOriginPatterns(List.of("*"));
        // preflight(OPTIONS) 포함 모든 HTTP 메서드 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Authorization 등 모든 요청 헤더 허용
        config.setAllowedHeaders(List.of("*"));
        // 쿠키/인증 정보 포함 요청 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 위 CORS 설정 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
