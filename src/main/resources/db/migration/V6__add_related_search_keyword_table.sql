CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE related_search_keyword (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_related_search_keyword_keyword_trgm
    ON related_search_keyword USING GIN (keyword gin_trgm_ops);