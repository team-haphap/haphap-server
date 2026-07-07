ALTER TABLE posting_stage ADD COLUMN IF NOT EXISTS expected_score INTEGER;

-- 기존 로우 백필 후 NOT NULL/DEFAULT 적용
-- (엔티티가 primitive int라 NULL 들어오면 Hibernate 매핑 시 런타임 예외 발생)
UPDATE posting_stage SET expected_score = 0 WHERE expected_score IS NULL;
ALTER TABLE posting_stage ALTER COLUMN expected_score SET NOT NULL;
ALTER TABLE posting_stage ALTER COLUMN expected_score SET DEFAULT 0;