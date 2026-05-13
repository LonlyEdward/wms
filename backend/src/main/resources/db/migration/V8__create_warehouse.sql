CREATE TABLE pick_lists (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            business_id UUID NOT NULL REFERENCES businesses(id),
                            order_id UUID UNIQUE NOT NULL REFERENCES orders(id),
                            assigned_to UUID REFERENCES users(id),
                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                            started_at TIMESTAMP,
                            completed_at TIMESTAMP,
                            dispatch_note_path TEXT,
                            pod_image_path TEXT,
                            pod_captured_at TIMESTAMP,
                            driver_id UUID REFERENCES users(id),
                            notes TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);