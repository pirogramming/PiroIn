package com.example.Piroin.project.domain.question.dto;

import com.example.Piroin.project.domain.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionResDTO {
    /*
    질문 등록 응답
    anonymous_no는 댓글 작성 시 부여되므로 여기서는 포함 X
    */
    @Getter
    @Builder
    public static class CreateRes {
        private Long id;
        private String content;
        private Boolean isSolved;
        private Integer likeCount;
        private LocalDateTime createdAt;

        // 엔티티 → DTO 변환 메서드
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
            String sessionDate,
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
}
