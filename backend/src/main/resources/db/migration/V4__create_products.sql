CREATE TABLE product_categories (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    business_id UUID NOT NULL REFERENCES businesses(id),
                                    name VARCHAR(100) NOT NULL,
                                    parent_id UUID REFERENCES product_categories(id),
                                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          business_id UUID NOT NULL REFERENCES businesses(id),
                          sku VARCHAR(100) NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          category_id UUID REFERENCES product_categories(id),
                          parent_id UUID REFERENCES products(id),
                          unit_of_measure VARCHAR(30) NOT NULL DEFAULT 'UNIT',
                          cost_price DECIMAL(15,2) NOT NULL DEFAULT 0,
                          sale_price DECIMAL(15,2) NOT NULL,
                          reorder_point INTEGER NOT NULL DEFAULT 10,
                          track_inventory BOOLEAN NOT NULL DEFAULT true,
                          is_active BOOLEAN NOT NULL DEFAULT true,
                          attributes JSONB,
                          image_url TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          UNIQUE(business_id, sku)
);