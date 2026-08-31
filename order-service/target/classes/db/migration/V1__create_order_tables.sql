CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rejection_reason VARCHAR(500),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'FULFILLED')),
    CONSTRAINT chk_orders_rejection_reason
        CHECK (
            (status = 'REJECTED' AND rejection_reason IS NOT NULL AND LENGTH(TRIM(rejection_reason)) > 0)
            OR (status <> 'REJECTED' AND rejection_reason IS NULL)
        )
);

CREATE TABLE order_lines (
    order_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    sku VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    PRIMARY KEY (order_id, line_number),
    CONSTRAINT fk_order_lines_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT chk_order_lines_line_number CHECK (line_number > 0),
    CONSTRAINT chk_order_lines_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_lines_unit_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_orders_customer_created_at
    ON orders (customer_id, created_at DESC);

CREATE INDEX idx_orders_status_created_at
    ON orders (status, created_at DESC);
