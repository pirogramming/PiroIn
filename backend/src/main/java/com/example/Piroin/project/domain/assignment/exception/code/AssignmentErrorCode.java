package com.example.Piroin.project.domain.assignment.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AssignmentErrorCode {

    ASSIGNMENT_CREATE_FAILED(
            HttpStatus.BAD_REQUEST,
            "ASSIGNMENT400",
            "과제 생성에 실패했습니다."
    ),

    ASSIGNMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ASSIGNMENT404",
            "해당 과제를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}