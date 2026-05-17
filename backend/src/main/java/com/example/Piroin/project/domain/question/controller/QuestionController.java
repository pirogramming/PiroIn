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
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_ROOM_OK,
                questionService.getQuestionRoom(sessionId, understandingIndex));
    }

    // 질문 상세 조회
    // GET /api/questions/{questionId}
    @GetMapping("/api/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResDTO.QuestionDetailResponse>> getQuestionDetail(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_DETAIL_OK,
                questionService.getQuestionDetail(questionId, userId));
    }

    // 질문 등록
    // POST /api/sessions/{sessionId}/questions
    @PostMapping("/api/sessions/{sessionId}/questions")
    public ResponseEntity<ApiResponse<QuestionResDTO.CreateRes>> createQuestion(
            @PathVariable Long sessionId,
            @RequestBody QuestionReqDTO.CreateReq request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_CREATED,
                questionService.createQuestion(sessionId, request, userId));
    }

    // 댓글/대댓글 등록
    // POST /api/questions/{questionId}/comments
    @PostMapping("/api/questions/{questionId}/comments")
    public ResponseEntity<ApiResponse<QuestionResDTO.CommentCreateRes>> createComment(
            @PathVariable Long questionId,
            @RequestBody QuestionReqDTO.CommentReq request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.COMMENT_CREATED,
                questionService.createComment(questionId, request, userId));
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
        return ResponseUtil.success(QuestionSuccessCode.UNDERSTANDING_RESPONSE_OK,
                questionService.respondUnderstandingCheck(sessionId, checkId, request, userId));
    }
}
