-- 이미지만 단독 업로드가 가능하도록 content NOT NULL 제약 해제
ALTER TABLE question ALTER COLUMN content DROP NOT NULL;
ALTER TABLE question_comment ALTER COLUMN content DROP NOT NULL;