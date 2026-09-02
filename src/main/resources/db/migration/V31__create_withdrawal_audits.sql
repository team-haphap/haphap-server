CREATE TABLE withdrawal_audits (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL,
                                   provider VARCHAR(20) NOT NULL,
                                   provider_id VARCHAR(255) NOT NULL,
                                   withdrawn_at TIMESTAMP NOT NULL,
                                   retention_until TIMESTAMP NOT NULL,
                                   CONSTRAINT fk_withdrawal_audits_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_withdrawal_audits_retention_until ON withdrawal_audits (retention_until);

ALTER TABLE users ADD COLUMN withdrawal_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN withdrawal_retry_count INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN withdrawal_requested_at TIMESTAMP NULL;

CREATE INDEX idx_users_withdrawal_status ON users (withdrawal_status);