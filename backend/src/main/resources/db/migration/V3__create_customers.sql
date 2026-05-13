CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           business_id UUID NOT NULL REFERENCES businesses(id),
                           user_id UUID REFERENCES users(id),
                           name VARCHAR(200) NOT NULL,
                           email VARCHAR(200) NOT NULL,
                           phone VARCHAR(30),
                           account_type VARCHAR(30) NOT NULL DEFAULT 'RETAIL',
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           credit_limit DECIMAL(15,2) DEFAULT 0,
                           payment_terms VARCHAR(20) DEFAULT 'NET_30',
                           notes TEXT,
                           tags TEXT[],
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE customer_addresses (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    business_id UUID NOT NULL REFERENCES businesses(id),
                                    customer_id UUID NOT NULL REFERENCES customers(id),
                                    label VARCHAR(50),
                                    street TEXT NOT NULL,
                                    city VARCHAR(100) NOT NULL,
                                    region VARCHAR(100),
                                    country VARCHAR(100) NOT NULL DEFAULT 'Tanzania',
                                    is_default BOOLEAN NOT NULL DEFAULT false,
                                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);