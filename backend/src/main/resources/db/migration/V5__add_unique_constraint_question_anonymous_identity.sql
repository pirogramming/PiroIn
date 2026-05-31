ALTER TABLE question_anonymous_identity
    ADD CONSTRAINT uq_question_anonymous_identity_question_user
    UNIQUE (question_id, user_id);