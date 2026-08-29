CREATE TABLE inventory_stock (
    sku VARCHAR(120) PRIMARY KEY,
    available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_reservation (
    event_id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('RESERVED', 'REJECTED')),
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE inventory_reservation_line (
    event_id UUID NOT NULL,
    line_no INTEGER NOT NULL,
    sku VARCHAR(120) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (event_id, line_no),
    CONSTRAINT fk_inventory_reservation_line_reservation
        FOREIGN KEY (event_id) REFERENCES inventory_reservation(event_id) ON DELETE CASCADE
);

CREATE TABLE processed_event (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_inventory_reservation_order_id
    ON inventory_reservation(order_id);

CREATE INDEX idx_inventory_reservation_status
    ON inventory_reservation(status);

CREATE INDEX idx_processed_event_processed_at
    ON processed_event(processed_at);
