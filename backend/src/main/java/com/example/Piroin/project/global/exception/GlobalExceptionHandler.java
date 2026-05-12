package com.example.Piroin.project.global.exception;

import com.example.Piroin.project.domain.attendance.dto.ApiResponse;
import com.example.Piroin.project.domain.question.exception.QuestionException;
import com.example.Piroin.project.domain.user.exception.InvalidLoginException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
전역 예외 처리 클래스
서비스에서 예외가 발생하면 컨트롤러까지 올라오고,
이 클래스가 자동으로 받아서 HTTP 응답으로 바꿔줌
*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    // QuestionException 처리
    @ExceptionHandler(QuestionException.class)
    public ResponseEntity<ApiResponse<?>> handleQuestionException(QuestionException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    // 로그인 실패
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLoginException(InvalidLoginException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
    }

    // 잘못된 요청 값
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    // 잘못된 상태
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    // 그 외 런타임 예외
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }
}
