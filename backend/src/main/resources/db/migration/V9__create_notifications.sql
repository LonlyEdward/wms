CREATE TABLE notification_log (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  business_id UUID NOT NULL REFERENCES businesses(id),
                                  recipient_id UUID REFERENCES users(id),
                                  recipient_email VARCHAR(200),
                                  recipient_phone VARCHAR(30),
                                  event_type VARCHAR(60) NOT NULL,
                                  channel VARCHAR(20) NOT NULL,
                                  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                  subject TEXT,
                                  body_preview TEXT,
                                  sent_at TIMESTAMP,
                                  error_message TEXT,
                                  retry_count INTEGER NOT NULL DEFAULT 0,
                                  reference_type VARCHAR(30),
                                  reference_id UUID,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE in_app_notifications (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      business_id UUID NOT NULL REFERENCES businesses(id),
                                      recipient_id UUID NOT NULL REFERENCES users(id),
                                      type VARCHAR(60) NOT NULL,
                                      title VARCHAR(200) NOT NULL,
                                      message TEXT NOT NULL,
                                      link TEXT,
                                      is_read BOOLEAN NOT NULL DEFAULT false,
                                      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_templates (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        business_id UUID NOT NULL REFERENCES businesses(id),
                                        event_type VARCHAR(60) NOT NULL,
                                        channel VARCHAR(20) NOT NULL,
                                        subject TEXT,
                                        body TEXT NOT NULL,
                                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        UNIQUE(business_id, event_type, channel)
);