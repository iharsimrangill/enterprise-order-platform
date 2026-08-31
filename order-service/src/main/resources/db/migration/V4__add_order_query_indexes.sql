CREATE INDEX idx_orders_customer_created_at
    ON orders (customer_id, created_at DESC);

CREATE INDEX idx_orders_status_created_at
    ON orders (status, created_at DESC);
