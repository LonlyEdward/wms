CREATE TABLE invoices (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          business_id UUID NOT NULL REFERENCES businesses(id),
                          invoice_number VARCHAR(50) UNIQUE NOT NULL,
                          order_id UUID REFERENCES orders(id),
                          customer_id UUID NOT NULL REFERENCES customers(id),
                          invoice_type VARCHAR(20) NOT NULL DEFAULT 'TAX',
                          status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
                          issue_date DATE NOT NULL,
                          due_date DATE NOT NULL,
                          subtotal DECIMAL(15,2) NOT NULL,
                          tax_rate DECIMAL(5,2) NOT NULL,
                          tax_amount DECIMAL(15,2) NOT NULL,
                          total_amount DECIMAL(15,2) NOT NULL,
                          amount_paid DECIMAL(15,2) NOT NULL DEFAULT 0,
                          amount_outstanding DECIMAL(15,2) NOT NULL,
                          pdf_path TEXT,
                          voided_by UUID REFERENCES users(id),
                          void_reason TEXT,
                          notes TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE invoice_items (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               business_id UUID NOT NULL REFERENCES businesses(id),
                               invoice_id UUID NOT NULL REFERENCES invoices(id),
                               description TEXT NOT NULL,
                               quantity DECIMAL(10,3) NOT NULL,
                               unit_price DECIMAL(15,2) NOT NULL,
                               line_total DECIMAL(15,2) NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          business_id UUID NOT NULL REFERENCES businesses(id),
                          invoice_id UUID NOT NULL REFERENCES invoices(id),
                          customer_id UUID NOT NULL REFERENCES customers(id),
                          amount DECIMAL(15,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          payment_method VARCHAR(30) NOT NULL,
                          gateway VARCHAR(30),
                          gateway_ref VARCHAR(200),
                          gateway_status VARCHAR(50),
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          paid_at TIMESTAMP,
                          recorded_by UUID REFERENCES users(id),
                          receipt_path TEXT,
                          notes TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);