CREATE TABLE admin (
                       id BIGSERIAL PRIMARY KEY,

                       login_id VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,

                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);