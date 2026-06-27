ALTER TABLE question
    ADD COLUMN admin_checked_at TIMESTAMP NULL,
    ADD COLUMN admin_checked_by BIGINT NULL;

-- 기존 질문은 운영진이 이미 확인한 것으로 처리하고,
-- 이후 새로 생성되는 질문만 admin_checked_at = NULL 상태로 남겨 NEW 표시 대상으로 삼는다.
UPDATE question
SET admin_checked_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
WHERE admin_checked_at IS NULL;
