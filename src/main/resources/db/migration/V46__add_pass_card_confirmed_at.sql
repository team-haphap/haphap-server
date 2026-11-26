ALTER TABLE registration
    ADD COLUMN pass_card_confirmed_at TIMESTAMP;

-- 기존에 이미 승인된 합격 결과는 이미 카드를 본 것으로 간주

UPDATE registration
SET pass_card_confirmed_at = NOW()
WHERE verification_status = 'APPROVED';

CREATE INDEX idx_registration_pass_card
    ON registration (user_id, verification_status, pass_card_confirmed_at);