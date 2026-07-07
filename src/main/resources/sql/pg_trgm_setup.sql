CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_company_name_trgm ON company USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_posting_title_trgm ON posting USING gin (title gin_trgm_ops);