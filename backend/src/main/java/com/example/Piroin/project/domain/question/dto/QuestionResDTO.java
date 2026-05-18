package com.example.Piroin.project.domain.question.dto;

import com.example.Piroin.project.domain.question.entity.Question;
import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class QuestionResDTO {
    // 질문 등록 응답
    @Getter
    @Builder
    public static class CreateRes {
        private Long id;
        private String content;
        private Boolean isSolved;
        private Integer likeCount;
        private LocalDateTime createdAt;

        public static CreateRes from(Question question) {
            return CreateRes.builder()
                    .id(question.getId())
                    .content(question.getContent())
                    .isSolved(question.getIsResolved())
                    .likeCount(question.getLikeCount())
                    .createdAt(question.getCreatedAt())
                    .build();
        }
    }

    // 댓글 등록 응답
    public record CommentCreateRes(
            Long commentId,
            Long questionId,
            String displayName,
            String content,
            LocalDateTime createdAt
    ) {
    }

    // 좋아요 토글 응답
    // isLiked: true면 좋아요 추가된 상태, false면 취소된 상태
    public record LikeRes(
            Long id,
            Integer likeCount,
            Boolean isLiked
    ) {
    }

    // 질문 상세 응답
    public record QuestionDetailResponse(
            Long questionId,
            String displayName,
            String content,
            String imageUrl,
            Boolean isResolved,
            Boolean isPopular,
            Integer likeCount,
            Boolean isLiked,
            LocalDateTime createdAt,
            List<CommentResponse> comments
    ) {
    }

    // 댓글 조회 응답 (상세 페이지용)
    public record CommentResponse(
            Long commentId,
            String displayName,
            String content,
            String imageUrl,
            LocalDateTime createdAt,
            List<CommentResponse> replies
    ) {
    }

    // 질문 방 전체 응답
    public record QuestionRoomResponse(
            SessionResponse session,
            UnderstandingSliceResponse understanding,
            QuestionGroupsResponse questions
    ) {
    }

    public record SessionResponse(
            Long sessionId,
            Integer week,
            String dayOfWeek,
            String dayPart,
            LocalDate sessionDate,
            String title
    ) {
    }

    public record UnderstandingSliceResponse(
            UnderstandingCheckResponse current,
            Integer currentIndex,
            Integer totalCount,
            Boolean hasOlder,
            Boolean hasNewer
    ) {
    }

    public record UnderstandingCheckResponse(
            Long checkId,
            String content,
            Integer understoodCount,
            Integer notUnderstoodCount,
            LocalDateTime createdAt
    ) {
    }

    public record QuestionGroupsResponse(
            List<QuestionSummaryResponse> popularQuestions,
            List<QuestionSummaryResponse> unresolvedQuestions,
            List<QuestionSummaryResponse> resolvedQuestions
    ) {
    }

    public record QuestionSummaryResponse(
            Long questionId,
            String content,
            String imageUrl,
            Boolean isResolved,
            Boolean isPopular,
            Integer likeCount,
            Integer commentCount,
            LocalDateTime createdAt
    ) {
    }

    public record UnderstandingResponseResult(
            Long checkId,
            UnderstandResChoice selectedChoice,
            Integer understoodCount,
            Integer notUnderstoodCount
    ) {
    }

    public record UnderstandingCheckCreateResponse(
            Long checkId,
            String content,
            Integer understoodCount,
            Integer notUnderstoodCount,
            LocalDateTime createdAt
    ) {
    }
}
