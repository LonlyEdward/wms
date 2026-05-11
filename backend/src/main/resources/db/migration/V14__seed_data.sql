-- Business
INSERT INTO businesses (id, name, slug, email, currency_code, tax_rate)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Dar es Salaam Wholesale Co.',
           'dsm-wholesale',
           'admin@dsmwholesale.co.tz',
           'TZS',
           18.00
       );

-- Admin user (password is BCrypt hash of 'demo123')
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000002',
             '00000000-0000-0000-0000-000000000001',
             'admin@demo.com',
             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oRhP5J2Ky',
             'Admin', 'User', 'ADMIN'
         );

-- Warehouse user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000003',
             '00000000-0000-0000-0000-000000000001',
             'warehouse@demo.com',
             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oRhP5J2Ky',
             'Warehouse', 'Staff', 'WAREHOUSE'
         );

-- Accounts user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000004',
             '00000000-0000-0000-0000-000000000001',
             'accounts@demo.com',
             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oRhP5J2Ky',
             'Accounts', 'Team', 'ACCOUNTS'
         );

-- Buyer user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000005',
             '00000000-0000-0000-0000-000000000001',
             'buyer@demo.com',
             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oRhP5J2Ky',
             'Buyer', 'Portal', 'BUYER'
         );

-- Product categories
INSERT INTO product_categories (id, business_id, name) VALUES
                                                           ('00000000-0000-0000-0001-000000000001',
                                                            '00000000-0000-0000-0000-000000000001', 'Beverages'),
                                                           ('00000000-0000-0000-0001-000000000002',
                                                            '00000000-0000-0000-0000-000000000001', 'Dry Foods'),
                                                           ('00000000-0000-0000-0001-000000000003',
                                                            '00000000-0000-0000-0000-000000000001', 'Household'),
                                                           ('00000000-0000-0000-0001-000000000004',
                                                            '00000000-0000-0000-0000-000000000001', 'Stationery');