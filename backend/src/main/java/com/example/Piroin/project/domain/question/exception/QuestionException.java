package com.example.Piroin.project.domain.question.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
Q&A 도메인 전용 커스텀 예외 클래스
서비스 코드에서 문제가 생겼을 때 이 예외를 던짐
GlobalExceptionHandler가 이 예외를 받아 HTTP 응답으로 변환
사용 예시: throw new QuestionException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
*/
@Getter
public class QuestionException extends RuntimeException {

    private final HttpStatus status; // 예: HttpStatus.NOT_FOUND(=404)

    public QuestionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}