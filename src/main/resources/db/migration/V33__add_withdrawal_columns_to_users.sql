-- 회원 탈퇴(즉시 처리) 및 애플 연동 해제에 필요한 컬럼
ALTER TABLE users ADD COLUMN withdrawn_at        TIMESTAMP(6) NULL;
ALTER TABLE users ADD COLUMN apple_refresh_token VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN withdrawal_status   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE users ADD COLUMN withdrawal_retry_count  INT          NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN withdrawal_requested_at TIMESTAMP(6) NULL;