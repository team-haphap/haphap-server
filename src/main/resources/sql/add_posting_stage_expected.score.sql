ALTER TABLE posting_stage ADD COLUMN IF NOT EXISTS expected_score INTEGER;

//혹시 요걸로 바꾸는 건 어떨까요?
//UPDATE posting_stage SET expected_score = 0 WHERE expected_score IS NULL;
//ALTER TABLE posting_stage ALTER COLUMN expected_score SET NOT NULL;
//ALTER TABLE posting_stage ALTER COLUMN expected_score SET DEFAULT 0;