CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       business_id UUID NOT NULL REFERENCES businesses(id),
                       email VARCHAR(200) UNIQUE NOT NULL,
                       password_hash VARCHAR(255),
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       role VARCHAR(30) NOT NULL,
                       oauth_provider VARCHAR(30),
                       oauth_subject VARCHAR(200),
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       last_login_at TIMESTAMP,
                       notif_email BOOLEAN NOT NULL DEFAULT true,
                       notif_sms BOOLEAN NOT NULL DEFAULT false,
                       notif_whatsapp BOOLEAN NOT NULL DEFAULT false,
                       phone VARCHAR(30),
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                business_id UUID NOT NULL REFERENCES businesses(id),
                                user_id UUID NOT NULL REFERENCES users(id),
                                token_hash VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT false,
                                user_agent TEXT,
                                ip_address VARCHAR(45),
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);