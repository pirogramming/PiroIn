package com.example.Piroin.project.domain.question.controller;

import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.exception.code.QuestionSuccessCode;
import com.example.Piroin.project.domain.question.service.QuestionService;
import com.example.Piroin.project.global.response.ApiResponse;
import com.example.Piroin.project.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    // 질문 목록 + 이해도 조회
    // GET /api/sessions/{sessionId}/questions?understandingIndex=0
    @GetMapping("/api/sessions/{sessionId}/questions")
    public ResponseEntity<ApiResponse<QuestionResDTO.QuestionRoomResponse>> getQuestionRoom(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int understandingIndex
    ) {
        QuestionResDTO.QuestionRoomResponse response =
                questionService.getQuestionRoom(sessionId, understandingIndex);

        return ResponseUtil.success(QuestionSuccessCode.QUESTION_ROOM_OK, response);
    }

    // 질문 상세 조회
    // GET /api/questions/{questionId}
    @GetMapping("/api/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResDTO.QuestionDetailResponse>> getQuestionDetail(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId
    ) {
        QuestionResDTO.QuestionDetailResponse response =
                questionService.getQuestionDetail(questionId, userId);

        return ResponseUtil.success(QuestionSuccessCode.QUESTION_DETAIL_OK, response);
    }

    // 이해도 체크 응답
    // POST /api/sessions/{sessionId}/understanding-checks/{checkId}/responses
    @PostMapping("/api/sessions/{sessionId}/understanding-checks/{checkId}/responses")
    public ResponseEntity<ApiResponse<QuestionResDTO.UnderstandingResponseResult>> respondUnderstandingCheck(
            @PathVariable Long sessionId,
            @PathVariable Long checkId,
            @RequestBody QuestionReqDTO.UnderstandingResponseReq request,
            @AuthenticationPrincipal Long userId
    ) {
        QuestionResDTO.UnderstandingResponseResult response =
                questionService.respondUnderstandingCheck(sessionId, checkId, request, userId);

        return ResponseUtil.success(QuestionSuccessCode.UNDERSTANDING_RESPONSE_OK, response);
    }

    // 질문 등록
    // POST /api/sessions/{sessionId}/questions
    @PostMapping("/api/sessions/{sessionId}/questions")
    public ResponseEntity<ApiResponse<QuestionResDTO.CreateRes>> createQuestion(
            @PathVariable Long sessionId,
            @RequestBody QuestionReqDTO.CreateReq request,
            @AuthenticationPrincipal Long userId
    ) {
        QuestionResDTO.CreateRes response =
                questionService.createQuestion(sessionId, request, userId);

        return ResponseUtil.success(QuestionSuccessCode.QUESTION_CREATED, response);
    }
}
