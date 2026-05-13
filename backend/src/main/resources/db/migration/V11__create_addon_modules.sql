CREATE TABLE price_lists (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             business_id UUID NOT NULL REFERENCES businesses(id),
                             name VARCHAR(100) NOT NULL,
                             is_default BOOLEAN NOT NULL DEFAULT false,
                             valid_from DATE,
                             valid_to DATE,
                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE price_list_items (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  business_id UUID NOT NULL REFERENCES businesses(id),
                                  price_list_id UUID NOT NULL REFERENCES price_lists(id),
                                  product_id UUID NOT NULL REFERENCES products(id),
                                  price DECIMAL(15,2) NOT NULL,
                                  min_quantity INTEGER NOT NULL DEFAULT 1,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE returns (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         business_id UUID NOT NULL REFERENCES businesses(id),
                         rma_number VARCHAR(50) UNIQUE NOT NULL,
                         order_id UUID NOT NULL REFERENCES orders(id),
                         customer_id UUID NOT NULL REFERENCES customers(id),
                         status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
                         reason TEXT NOT NULL,
                         notes TEXT,
                         approved_by UUID REFERENCES users(id),
                         credit_note_id UUID REFERENCES invoices(id),
                         created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE return_items (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              business_id UUID NOT NULL REFERENCES businesses(id),
                              return_id UUID NOT NULL REFERENCES returns(id),
                              order_item_id UUID NOT NULL REFERENCES order_items(id),
                              quantity INTEGER NOT NULL,
                              condition VARCHAR(30),
                              restock BOOLEAN NOT NULL DEFAULT false,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);