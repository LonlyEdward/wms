CREATE TABLE password_reset_tokens (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id UUID NOT NULL REFERENCES users(id),
                                       token_hash VARCHAR(255) NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       used BOOLEAN NOT NULL DEFAULT false,
                                       created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reset_token_hash
    ON password_reset_tokens(token_hash);