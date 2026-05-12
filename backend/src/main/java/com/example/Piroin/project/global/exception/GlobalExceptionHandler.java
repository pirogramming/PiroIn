package com.example.Piroin.project.global.exception;

import com.example.Piroin.project.domain.curriculum.exception.CurriculumException;
import com.example.Piroin.project.domain.question.exception.QuestionException;
import com.example.Piroin.project.domain.user.exception.InvalidLoginException;
import com.example.Piroin.project.domain.user.exception.code.UserErrorCode;
import com.example.Piroin.project.global.response.ApiResponse;
import com.example.Piroin.project.global.response.ResponseUtil;
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

    @ExceptionHandler(QuestionException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuestionException(QuestionException e) {
        return ResponseUtil.failure(e.getStatus(), "QUESTION_ERROR", e.getMessage());
    }

    @ExceptionHandler(CurriculumException.class)
    public ResponseEntity<ApiResponse<Void>> handleCurriculumException(CurriculumException e) {
        return ResponseUtil.failure(e.getStatus(), "CURRICULUM_ERROR", e.getMessage());
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLoginException(InvalidLoginException e) {
        return ResponseUtil.failure(UserErrorCode.INVALID_LOGIN, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseUtil.failure(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        return ResponseUtil.failure(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseUtil.failure(HttpStatus.BAD_REQUEST, "RUNTIME_ERROR", e.getMessage());
    }
}
