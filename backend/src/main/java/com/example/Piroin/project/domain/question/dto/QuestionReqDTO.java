package com.example.Piroin.project.domain.question.dto;

import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuestionReqDTO {

    // 질문 등록 요청
    @Getter
    @NoArgsConstructor
    public static class CreateReq {
        private String content;
        private String imageUrl;
    }

    // 질문 수정 요청
    @Getter
    @NoArgsConstructor
    public static class UpdateReq {
        private String content;
    }

    // 댓글/대댓글 등록 요청
    // parentCommentId가 null이면 일반 댓글, 값이 있으면 대댓글
    @Getter
    @NoArgsConstructor
    public static class CommentReq {
        private String content;
        private String imageUrl;
        private Long parentCommentId;  // 대댓글일 때만 값이 있음, 일반 댓글이면 null
    }

    // 이해도 체크 응답 요청
    @Getter
    @NoArgsConstructor
    public static class UnderstandingResponseReq {
        private UnderstandResChoice choice;
    }

    // 이해도 체크 생성 요청
    @Getter
    @NoArgsConstructor
    public static class UnderstandingCheckCreateReq {
        private String content;
    }
}
