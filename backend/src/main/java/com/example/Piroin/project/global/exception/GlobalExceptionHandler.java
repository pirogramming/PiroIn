package com.example.Piroin.project.global.exception;

import com.example.Piroin.project.domain.question.exception.QuestionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/*
전역 예외 처리 클래스
서비스에서 예외가 발생하면 컨트롤러까지 올라오고,
이 클래스가 자동으로 받아서 HTTP 응답으로 바꿔줌
try-catch 없이도 깔끔하게 에러 처리 가능
*/
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*
    QuestionException 처리
    예: 질문을 찾을 수 없을 때 → 404, "질문을 찾을 수 없습니다."
    */
    @ExceptionHandler(QuestionException.class)
    public ResponseEntity<Map<String, String>> handleQuestionException(QuestionException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }
}