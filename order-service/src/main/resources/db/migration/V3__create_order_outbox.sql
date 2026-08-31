CREATE TABLE order_outbox (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NULL,
    attempts INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_order_outbox_unpublished
    ON order_outbox (created_at)
    WHERE published_at IS NULL;
