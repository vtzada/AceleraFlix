CREATE TABLE refresh_token
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    jti             UUID        NOT NULL UNIQUE,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expires_at      TIMESTAMP   NOT NULL,
    revoked         BOOLEAN     NOT NULL DEFAULT FALSE,
    replaced_by_jti UUID,
    created_at      TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token (expires_at);
