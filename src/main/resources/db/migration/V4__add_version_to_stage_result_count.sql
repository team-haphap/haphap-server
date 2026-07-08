ALTER TABLE stage_result_count
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE stage_result_count
    ALTER COLUMN version DROP DEFAULT;