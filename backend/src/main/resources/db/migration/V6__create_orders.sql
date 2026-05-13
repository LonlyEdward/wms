CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        business_id UUID NOT NULL REFERENCES businesses(id),
                        order_number VARCHAR(50) UNIQUE NOT NULL,
                        customer_id UUID NOT NULL REFERENCES customers(id),
                        delivery_address_id UUID REFERENCES customer_addresses(id),
                        status VARCHAR(30) NOT NULL DEFAULT 'NEW',
                        order_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
                        placed_by UUID REFERENCES users(id),
                        notes TEXT,
                        is_flagged BOOLEAN NOT NULL DEFAULT false,
                        flag_reason TEXT,
                        confirmed_at TIMESTAMP,
                        dispatched_at TIMESTAMP,
                        delivered_at TIMESTAMP,
                        subtotal DECIMAL(15,2) NOT NULL DEFAULT 0,
                        tax_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                        total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                        delivery_address_snapshot JSONB,
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             business_id UUID NOT NULL REFERENCES businesses(id),
                             order_id UUID NOT NULL REFERENCES orders(id),
                             product_id UUID NOT NULL REFERENCES products(id),
                             product_snapshot JSONB NOT NULL,
                             quantity_ordered INTEGER NOT NULL,
                             quantity_picked INTEGER NOT NULL DEFAULT 0,
                             unit_price DECIMAL(15,2) NOT NULL,
                             line_total DECIMAL(15,2) NOT NULL,
                             notes TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      business_id UUID NOT NULL REFERENCES businesses(id),
                                      order_id UUID NOT NULL REFERENCES orders(id),
                                      from_status VARCHAR(30),
                                      to_status VARCHAR(30) NOT NULL,
                                      changed_by UUID REFERENCES users(id),
                                      reason TEXT,
                                      changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);