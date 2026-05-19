package com.example.Piroin.project.domain.curriculum.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CurriculumErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CURRICULUM401",
            "사용자를 찾을 수 없습니다."
    ),

    SESSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CURRICULUM402",
            "세션을 찾을 수 없습니다."
    ),

    STUDY_SESSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CURRICULUM404",
            "해당 스터디 세션이 존재하지 않습니다."
    ),

    SESSION_DATE_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "CURRICULUM405",
            "해당 날짜의 세션이 없습니다. 세션을 먼저 생성해주세요."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}