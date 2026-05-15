package com.example.Piroin.project.domain.question.dto;

import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuestionReqDTO {
    /*
    질문 등록 요청 바디
    프론트에서 { "content": "질문 내용" } 형태로 전달
    */
    @Getter
    @NoArgsConstructor
    public static class CreateReq {
        private String content;
    }

    /*
    이해도 체크 응답 요청 바디
    프론트에서 { "choice": "UNDERSTOOD" } 또는 { "choice": "NOT_UNDERSTOOD" } 형태로 전달
    */
    @Getter
    @NoArgsConstructor
    public static class UnderstandingResponseReq {
        private UnderstandResChoice choice;
    }
}
