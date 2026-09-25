CREATE TABLE withdrawal_reasons (
                                    id          BIGSERIAL PRIMARY KEY,
                                    reason      VARCHAR(30)  NOT NULL,
                                    etc_reason  VARCHAR(200),
                                    created_at  TIMESTAMP(6) NOT NULL,
                                    updated_at  TIMESTAMP(6)
);
