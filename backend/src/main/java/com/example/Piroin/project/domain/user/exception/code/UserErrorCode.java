package com.example.Piroin.project.domain.user.exception.code;

import com.example.Piroin.project.global.response.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseCode {
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "USER401", "로그인에 실패했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "USER400", "잘못된 사용자 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
