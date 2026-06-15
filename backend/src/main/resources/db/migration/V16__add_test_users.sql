-- Admin test user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000006',
             '00000000-0000-0000-0000-000000000001',
             'admintest2@demo.com',
             '$2a$12$k8zyLjRq9EdVRFV/ahACe.L5srnprN2raUge2/HTNxyMbOLSSyqdO',
             'Admin', 'Test', 'ADMIN'
         );

-- Warehouse test user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000007',
             '00000000-0000-0000-0000-000000000001',
             'warehousetest@demo.com',
             '$2a$12$k8zyLjRq9EdVRFV/ahACe.L5srnprN2raUge2/HTNxyMbOLSSyqdO',
             'Warehouse', 'Test', 'WAREHOUSE'
         );

-- Accounts test user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000008',
             '00000000-0000-0000-0000-000000000001',
             'accountstest@demo.com',
             '$2a$12$k8zyLjRq9EdVRFV/ahACe.L5srnprN2raUge2/HTNxyMbOLSSyqdO',
             'Accounts', 'Test', 'ACCOUNTS'
         );

-- Buyer test user
INSERT INTO users (
    id, business_id, email, password_hash,
    first_name, last_name, role
) VALUES (
             '00000000-0000-0000-0000-000000000009',
             '00000000-0000-0000-0000-000000000001',
             'buyertest@demo.com',
             '$2a$12$k8zyLjRq9EdVRFV/ahACe.L5srnprN2raUge2/HTNxyMbOLSSyqdO',
             'Buyer', 'Test', 'BUYER'
         );