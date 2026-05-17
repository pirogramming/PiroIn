package com.example.Piroin.project.global.jwt;

import com.example.Piroin.project.global.response.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements BaseCode {
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT4011", "만료된 토큰입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "JWT4012", "유효하지 않은 토큰입니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "JWT4013", "토큰이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
