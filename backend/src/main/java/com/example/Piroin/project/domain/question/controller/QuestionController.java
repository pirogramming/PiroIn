package com.example.Piroin.project.domain.question.controller;

import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.exception.code.QuestionSuccessCode;
import com.example.Piroin.project.domain.question.service.QuestionService;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.global.response.ApiResponse;
import com.example.Piroin.project.global.response.ResponseUtil;
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
 
    // 질문 목록 + 이해도 조회
    // GET /api/sessions/{sessionId}/questions?understandingIndex=0
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<ApiResponse<QuestionResDTO.QuestionRoomResponse>> getQuestionRoom(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int understandingIndex
    ) {
        QuestionResDTO.QuestionRoomResponse response =
                questionService.getQuestionRoom(sessionId, understandingIndex);
 
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_ROOM_OK, response);
    }
 
    // 질문 등록
    // POST /api/sessions/{sessionId}/questions
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<ApiResponse<QuestionResDTO.CreateRes>> createQuestion(
            @PathVariable Long sessionId,
            @RequestBody QuestionReqDTO.CreateReq request,
            HttpSession httpSession
    ) {
        User loginUser = (User) httpSession.getAttribute("loginUser");
 
        QuestionResDTO.CreateRes response =
                questionService.createQuestion(sessionId, request, loginUser);
 
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_CREATED, response);
    }
}
