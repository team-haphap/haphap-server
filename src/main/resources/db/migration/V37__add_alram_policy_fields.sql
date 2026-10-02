-- 알림 정책 개편: (공고x전형) 최초 합격 승인 1회, 공고당 최초 마감 1회 알림을 위한 중복방지 컬럼.
-- 값이 채워지면 그 뒤로 몇 번을 재평가하든 다시 발송하지 않는다.

ALTER TABLE posting_stage
    ADD COLUMN pass_alram_sent_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE posting
    ADD COLUMN closed_alram_sent_at TIMESTAMP(6) WITHOUT TIME ZONE;

-- 마감 알림(스케줄러 트리거)은 특정 등록자가 없어서 registrant_member_id를 채울 수 없다.
ALTER TABLE alram_failure
    ALTER COLUMN registrant_member_id DROP NOT NULL;
