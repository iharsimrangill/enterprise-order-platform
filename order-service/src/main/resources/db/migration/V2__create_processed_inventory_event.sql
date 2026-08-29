CREATE TABLE processed_inventory_event (
    event_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_processed_inventory_event_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT chk_processed_inventory_event_type
        CHECK (event_type IN ('inventory.reserved', 'inventory.rejected'))
);

CREATE INDEX idx_processed_inventory_event_order
    ON processed_inventory_event (order_id, processed_at DESC);
