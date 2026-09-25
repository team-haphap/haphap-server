ALTER TABLE users DROP COLUMN withdrawal_retry_count;
ALTER TABLE users DROP COLUMN withdrawal_requested_at;
DROP TABLE withdrawal_audits;