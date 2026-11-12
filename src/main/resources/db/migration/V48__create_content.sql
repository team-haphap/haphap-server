CREATE TABLE content (
                         id         BIGSERIAL PRIMARY KEY,
                         created_at TIMESTAMP(6),
                         updated_at TIMESTAMP(6),
                         user_id    BIGINT REFERENCES users (id),      -- 탈퇴 비식별화를 위해 NULL 허용
                         posting_id BIGINT REFERENCES posting (id),    -- 후기 대상 공고
                         type       VARCHAR(20)  NOT NULL,
                         title      VARCHAR(100) NOT NULL,
                         body       TEXT         NOT NULL
);

CREATE INDEX idx_content_type_id ON content (type, id DESC);  -- 타입별 최신순 탐색용
CREATE INDEX idx_content_user_id ON content (user_id);        -- 내가 작성한 콘텐츠용