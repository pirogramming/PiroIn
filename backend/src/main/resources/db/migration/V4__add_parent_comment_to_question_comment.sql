-- question_comment 테이블에 대댓글을 위한 parent_comment_id 컬럼 추가
-- Issue #4에서 QuestionComment 엔티티에 parentComment 필드가 추가되었는데 DB에 반영되지 않아 이 마이그레이션으로 동기화

ALTER TABLE question_comment
    ADD COLUMN parent_comment_id BIGINT,
    ADD CONSTRAINT fk_question_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES question_comment (id);