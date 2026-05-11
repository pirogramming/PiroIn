package com.example.Piroin.project.domain.question.dto;

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
}