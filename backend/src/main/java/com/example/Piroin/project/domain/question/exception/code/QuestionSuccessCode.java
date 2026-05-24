package com.example.Piroin.project.domain.question.exception.code;

import com.example.Piroin.project.global.response.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionSuccessCode implements BaseCode {

    QUESTION_ROOM_OK(HttpStatus.OK, "QUESTION200_1", "질문 방 조회에 성공했습니다."),
    QUESTION_DETAIL_OK(HttpStatus.OK, "QUESTION200_2", "질문 상세 조회에 성공했습니다."),
    LIKE_TOGGLED(HttpStatus.OK, "QUESTION200_3", "좋아요가 처리되었습니다."),
    UNDERSTANDING_RESPONSE_OK(HttpStatus.OK, "QUESTION200_4", "이해도 응답이 반영되었습니다."),
    QUESTION_UPDATED(HttpStatus.OK, "QUESTION200_5", "질문이 수정되었습니다."),
    QUESTION_CREATED(HttpStatus.CREATED, "QUESTION201_1", "질문이 등록되었습니다."),
    COMMENT_CREATED(HttpStatus.CREATED, "QUESTION201_2", "댓글이 등록되었습니다."),
    UNDERSTANDING_CHECK_CREATED(HttpStatus.CREATED, "QUESTION201_3", "이해도 체크가 생성되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
