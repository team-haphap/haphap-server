ALTER TABLE registration
    ADD COLUMN verification_round        INT       NOT NULL DEFAULT 1,
    ADD COLUMN verification_submitted_at TIMESTAMP,
    ADD COLUMN verification_resolved_at  TIMESTAMP;

UPDATE registration
SET verification_submitted_at = created_at
WHERE verification_status <> 'NOT_REQUIRED';

UPDATE registration
SET verification_resolved_at = updated_at
WHERE verification_status IN ('APPROVED', 'REJECTED');

CREATE INDEX idx_reg_user_verification
    ON registration (user_id, verification_status);