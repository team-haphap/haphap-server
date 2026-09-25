-- 전형 이동 신정책(1시간 5건 + 전이별 최소 시차) 전환 준비.
-- 스키마만 추가하고 기존 로직/데이터는 건드리지 않는다. 값 채우기와 로직 연결은 다음 단계에서 진행.

ALTER TABLE posting_stage
    ADD COLUMN stage_type VARCHAR(30),
    ADD COLUMN moved_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE posting
    ADD COLUMN current_stage_id BIGINT,
    ADD CONSTRAINT fk_posting_current_stage FOREIGN KEY (current_stage_id) REFERENCES posting_stage (id);
