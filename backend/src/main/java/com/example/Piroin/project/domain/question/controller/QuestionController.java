package com.example.Piroin.project.domain.question.controller;

import com.example.Piroin.project.domain.question.dto.QuestionReqDTO;
import com.example.Piroin.project.domain.question.dto.QuestionResDTO;
import com.example.Piroin.project.domain.question.exception.code.QuestionSuccessCode;
import com.example.Piroin.project.domain.question.service.QuestionService;
import com.example.Piroin.project.global.response.ApiResponse;
import com.example.Piroin.project.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    // 질문 목록 실시간 이벤트 구독
    // GET /api/sessions/{sessionId}/questions/events
    // text/event-stream으로 연결을 유지하며, 댓글 생성 같은 목록 갱신 이벤트를 받는다.
    // 인증 헤더가 필요하므로 프론트에서는 기본 EventSource 대신 fetch 기반 SSE 클라이언트로 구독한다.
    @GetMapping(value = "/api/sessions/{sessionId}/questions/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeQuestionEvents(@PathVariable Long sessionId) {
        return questionService.subscribeQuestionEvents(sessionId);
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

    // 좋아요 토글
    // POST /api/questions/{questionId}/like
    @PostMapping("/api/questions/{questionId}/like")
    public ResponseEntity<ApiResponse<QuestionResDTO.LikeRes>> toggleLike(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.LIKE_TOGGLED,
                questionService.toggleLike(questionId, userId));
    }

    // 질문 수정
    // PATCH /api/questions/{questionId}/modify
    @PatchMapping("/api/questions/{questionId}/modify")
    public ResponseEntity<ApiResponse<QuestionResDTO.UpdateDeleteRes>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionReqDTO.UpdateReq request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_UPDATED,
                questionService.updateQuestion(questionId, request, userId));
    }

    // 질문 삭제
    // DELETE /api/questions/{questionId}
    @DeleteMapping("/api/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResDTO.UpdateDeleteRes>> deleteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_DELETED,
                questionService.deleteQuestion(questionId, userId));
    }

    // 질문 상태 완료 전환 (관리자 전용)
    // PATCH /api/questions/{questionId}/status
    @PatchMapping("/api/questions/{questionId}/status")
    public ResponseEntity<ApiResponse<QuestionResDTO.StatusUpdateRes>> updateQuestionStatus(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.QUESTION_STATUS_UPDATED,
                questionService.updateQuestionStatus(questionId, userId));
    }

    // 이해도 체크 생성
    // POST /api/sessions/{sessionId}/understanding-checks
    @PostMapping("/api/sessions/{sessionId}/understanding-checks")
    public ResponseEntity<ApiResponse<QuestionResDTO.UnderstandingCheckCreateResponse>> createUnderstandingCheck(
            @PathVariable Long sessionId,
            @RequestBody QuestionReqDTO.UnderstandingCheckCreateReq request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseUtil.success(QuestionSuccessCode.UNDERSTANDING_CHECK_CREATED,
                questionService.createUnderstandingCheck(sessionId, request, userId));
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
