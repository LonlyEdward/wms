CREATE TABLE businesses (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(200) NOT NULL,
                            slug VARCHAR(100) UNIQUE NOT NULL,
                            email VARCHAR(200) NOT NULL,
                            phone VARCHAR(30),
                            address TEXT,
                            tax_number VARCHAR(50),
                            currency_code CHAR(3) NOT NULL DEFAULT 'TZS',
                            tax_rate DECIMAL(5,2) NOT NULL DEFAULT 18.00,
                            is_active BOOLEAN NOT NULL DEFAULT true,
                            plan VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
                            modules_enabled JSONB,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);