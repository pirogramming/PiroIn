package com.example.Piroin.project.domain.curriculum.exception.code;

import com.example.Piroin.project.global.response.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CurriculumSuccessCode implements BaseCode {
    ACTIVE_SESSION_LIST_OK(HttpStatus.OK, "SESSION200_1", "활성 세션 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
