package com.example.Piroin.project.domain.question.controller;

import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.service.QuestionService;
import com.example.Piroin.project.domain.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    /*
    질문 등록
    POST /api/sessions/{sessionId}/questions
    
    @param sessionId   URL 경로의 세션 ID
    @param request     요청 바디 { "content": "..." }
    @param httpSession 현재 HTTP 세션 (로그인 유저 확인용)
    */
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<QuestionResDTO.CreateRes> createQuestion(
            @PathVariable Long sessionId,
            @RequestBody QuestionReqDTO.CreateReq request,
            HttpSession httpSession
    ) {
        User loginUser = (User) httpSession.getAttribute("loginUser");

        QuestionResDTO.CreateRes response =
                questionService.createQuestion(sessionId, request, loginUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}