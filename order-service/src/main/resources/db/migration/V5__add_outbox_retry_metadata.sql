ALTER TABLE order_outbox
    ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN last_error VARCHAR(1000);

CREATE INDEX idx_order_outbox_retry
    ON order_outbox (next_attempt_at, created_at)
    WHERE published_at IS NULL;
