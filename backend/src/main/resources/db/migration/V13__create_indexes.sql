-- Orders
CREATE INDEX idx_orders_business_status
    ON orders(business_id, status);
CREATE INDEX idx_orders_customer
    ON orders(customer_id);
CREATE INDEX idx_orders_created
    ON orders(business_id, created_at DESC);

-- Invoices
CREATE INDEX idx_invoices_business_status
    ON invoices(business_id, status);
CREATE INDEX idx_invoices_due_date
    ON invoices(business_id, due_date)
    WHERE status NOT IN ('PAID', 'VOIDED');
CREATE INDEX idx_invoices_customer
    ON invoices(customer_id);

-- Stock
CREATE INDEX idx_stock_product
    ON stock_movements(product_id);
CREATE INDEX idx_stock_business
    ON stock_movements(business_id);

-- Audit log
CREATE INDEX idx_audit_entity
    ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_occurred
    ON audit_log(business_id, occurred_at DESC);

-- Users
CREATE INDEX idx_users_email
    ON users(email);
CREATE INDEX idx_users_business
    ON users(business_id);

-- Notifications
CREATE INDEX idx_notif_retry
    ON notification_log(status, retry_count)
    WHERE status = 'FAILED';
CREATE INDEX idx_inapp_recipient
    ON in_app_notifications(recipient_id, is_read);