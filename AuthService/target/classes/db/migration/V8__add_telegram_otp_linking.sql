ALTER TABLE tbl_users
    ADD COLUMN telegram_chat_id VARCHAR(64),
    ADD COLUMN otp_delivery_channel VARCHAR(16) NOT NULL DEFAULT 'EMAIL';

CREATE UNIQUE INDEX uq_tbl_users_telegram_chat_id
    ON tbl_users (telegram_chat_id)
    WHERE telegram_chat_id IS NOT NULL;

CREATE TABLE telegram_link_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES tbl_users(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
);

CREATE INDEX idx_telegram_link_tokens_user_id
    ON telegram_link_tokens (user_id);