ALTER TABLE question_anonymous_identity
    ADD COLUMN role VARCHAR(20);

UPDATE question_anonymous_identity identity
SET role = users.role
FROM users
WHERE identity.user_id = users.id
  AND identity.role IS NULL;

-- Normalize any duplicate numbers that may have been created while the role-scoped constraint was absent.
WITH ranked_identity AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY question_id, role
               ORDER BY anonymous_no ASC, created_at ASC, id ASC
           ) AS next_anonymous_no
    FROM question_anonymous_identity
)
UPDATE question_anonymous_identity identity
SET anonymous_no = ranked_identity.next_anonymous_no
FROM ranked_identity
WHERE identity.id = ranked_identity.id;

ALTER TABLE question_anonymous_identity
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE question_anonymous_identity
    ADD CONSTRAINT chk_question_anonymous_identity_role
    CHECK (role IN ('ADMIN', 'MEMBER'));

ALTER TABLE question_anonymous_identity
    ADD CONSTRAINT uq_question_anonymous_identity_question_role_no
    UNIQUE (question_id, role, anonymous_no);
