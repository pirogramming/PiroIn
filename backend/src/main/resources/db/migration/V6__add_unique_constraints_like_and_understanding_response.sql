-- QuestionLike: 같은 유저가 같은 질문에 좋아요를 중복으로 누르는 것을 DB 레벨에서 차단
ALTER TABLE question_like
    ADD CONSTRAINT uq_question_like_question_user
    UNIQUE (question_id, user_id);

-- UnderstandingResponse: 같은 유저가 같은 이해도 체크에 중복 응답하는 것을 DB 레벨에서 차단
ALTER TABLE understanding_response
    ADD CONSTRAINT uq_understanding_response_check_user
    UNIQUE (check_id, user_id);