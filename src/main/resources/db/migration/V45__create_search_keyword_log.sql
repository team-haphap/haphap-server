CREATE TABLE search_keyword_log (
                                    id          BIGSERIAL PRIMARY KEY,
                                    keyword     VARCHAR(255) NOT NULL,
                                    searched_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_search_keyword_log_searched_at ON search_keyword_log (searched_at);