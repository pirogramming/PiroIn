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
            Boolean isResolved,
            LocalDateTime createdAt
    ) {
    }

    // 댓글 수정/삭제 응답
    public record CommentUpdateDeleteRes(
            Long commentId,
            String content,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
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

    // 질문 수정/삭제 응답 (형태가 동일해서 하나로 공유)
    // deletedAt에 값이 있으면 삭제된 상태
    public record UpdateDeleteRes(
            Long id,
            String content,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
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
            Boolean isMine,
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

    // 질문 상태 변경 응답
    public record StatusUpdateRes(
            Long id,
            Boolean isResolved,
            LocalDateTime updatedAt
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

    // 질문방 이해도 체크 바 응답. 프론트는 이 값들로 "이해했다 (13/29)"와 오른쪽 O/X 숫자를 그린다.
    public record UnderstandingCheckResponse(
            Long checkId,
            String content,
            // 화면의 "13/29" 중 13: O 응답 수 + X 응답 수
            Integer respondedCount,
            // 화면의 "13/29" 중 29: 해당 세션에 대응되는 출석 회차의 출석 인원
            Integer attendanceCount,
            // 오른쪽 O 뱃지 숫자
            Integer understoodCount,
            // 오른쪽 X 뱃지 숫자
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
            Boolean isLiked,
            Boolean isMine,
            Integer likeCount,
            Integer commentCount,
            // 댓글이 없으면 빈 배열로 내려가며, 프론트는 빈 배열일 때 미리보기 영역을 숨긴다.
            List<PreviewCommentResponse> previewComments,
            LocalDateTime createdAt
    ) {
    }

    // 질문 목록용 댓글 미리보기 응답. 메인 목록에서는 대댓글 없이 최상위 댓글만 보여준다.
    public record PreviewCommentResponse(
            Long commentId,
            String displayName,
            String content,
            // true면 프론트는 미리보기 댓글에 "사진 보기" 버튼을 노출하고 질문 상세 페이지로 이동시킨다.
            Boolean hasImage,
            LocalDateTime createdAt
    ) {
    }

    // 댓글 생성 시 SSE로 내려가는 목록 갱신 이벤트 응답
    public record CommentCreatedEvent(
            String type,
            Long sessionId,
            Long questionId,
            Integer commentCount,
            List<PreviewCommentResponse> previewComments
    ) {
    }

    // O/X 클릭 직후 응답. selectedChoice가 null이면 같은 선택지를 다시 눌러 취소된 상태다.
    public record UnderstandingResponseResult(
            Long checkId,
            UnderstandResChoice selectedChoice,
            // 화면의 "13/29" 중 13: O 응답 수 + X 응답 수
            Integer respondedCount,
            // 화면의 "13/29" 중 29: 해당 세션에 대응되는 출석 회차의 출석 인원
            Integer attendanceCount,
            // 오른쪽 O 뱃지 숫자
            Integer understoodCount,
            // 오른쪽 X 뱃지 숫자
            Integer notUnderstoodCount
    ) {
    }

    // 운영진이 이해도 체크를 생성했을 때의 초기 응답. 생성 직후에는 O/X 응답자가 없어서 카운트가 0이다.
    public record UnderstandingCheckCreateResponse(
            Long checkId,
            String content,
            // 생성 직후에는 0
            Integer respondedCount,
            // 생성 응답에서는 질문방 조회 맥락이 아니므로 null로 내려간다.
            Integer attendanceCount,
            // 생성 직후에는 0
            Integer understoodCount,
            // 생성 직후에는 0
            Integer notUnderstoodCount,
            LocalDateTime createdAt
    ) {
    }
}
