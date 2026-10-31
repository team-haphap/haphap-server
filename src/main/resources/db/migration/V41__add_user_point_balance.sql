ALTER TABLE users
    ADD COLUMN point_balance INTEGER NOT NULL DEFAULT 0;

UPDATE users
SET point_balance = 300
WHERE withdrawal_status = 'ACTIVE';
