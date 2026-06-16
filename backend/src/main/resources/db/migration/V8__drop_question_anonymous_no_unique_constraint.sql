-- Anonymous numbers are scoped by role, so member #1 and admin #1 can coexist in the same question.
-- Drop the legacy question_id + anonymous_no constraint if it exists.
ALTER TABLE question_anonymous_identity
    DROP CONSTRAINT IF EXISTS uq_question_anon_question_no;
