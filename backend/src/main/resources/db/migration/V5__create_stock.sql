CREATE TABLE stock_movements (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 business_id UUID NOT NULL REFERENCES businesses(id),
                                 product_id UUID NOT NULL REFERENCES products(id),
                                 movement_type VARCHAR(30) NOT NULL,
                                 quantity INTEGER NOT NULL,
                                 quantity_before INTEGER NOT NULL,
                                 quantity_after INTEGER NOT NULL,
                                 reference_type VARCHAR(30),
                                 reference_id UUID,
                                 reason TEXT,
                                 performed_by UUID REFERENCES users(id),
                                 created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);