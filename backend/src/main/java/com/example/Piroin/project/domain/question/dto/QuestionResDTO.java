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

    // 질문 상세 응답
    // GET /api/questions/{questionId}
    public record QuestionDetailResponse(
            Long questionId,
            String displayName,   // "작성자" 고정 (질문자는 항상 작성자로 표시)
            String content,
            String imageUrl,
            Boolean isResolved,
            Boolean isPopular,
            Integer likeCount,
            Boolean isLiked,      // 현재 로그인 유저의 좋아요 여부
            LocalDateTime createdAt,
            List<CommentResponse> comments
    ) {
    }

    // 댓글 및 대댓글 응답
    // replies가 비어 있으면 일반 댓글, 값이 있으면 해당 댓글의 대댓글 목록
    public record CommentResponse(
            Long commentId,
            String displayName,   // "작성자" / "익명N" / "운영진N"
            String content,
            String imageUrl,
            LocalDateTime createdAt,
            List<CommentResponse> replies  // 대댓글 목록 (최상위 댓글에만 값 있음)
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
            String title,
            String description,
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
}
