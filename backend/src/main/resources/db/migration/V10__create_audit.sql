CREATE TABLE audit_log (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           business_id UUID REFERENCES businesses(id),
                           user_id UUID REFERENCES users(id),
                           action VARCHAR(60) NOT NULL,
                           entity_type VARCHAR(60) NOT NULL,
                           entity_id UUID NOT NULL,
                           old_value JSONB,
                           new_value JSONB,
                           ip_address VARCHAR(45),
                           user_agent TEXT,
                           occurred_at TIMESTAMP NOT NULL DEFAULT NOW()
);