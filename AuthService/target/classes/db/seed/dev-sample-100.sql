-- Local development sample data only. Synthetic accounts are disabled.
-- Adds localized text fields and guards each seed insert with a stable marker.
ALTER TABLE tbl_roles
    ADD COLUMN IF NOT EXISTS name_en varchar(64),
    ADD COLUMN IF NOT EXISTS name_kh varchar(128),
    ADD COLUMN IF NOT EXISTS description_en text,
    ADD COLUMN IF NOT EXISTS description_kh text;
ALTER TABLE tbl_user_profiles
    ADD COLUMN IF NOT EXISTS first_name_en text,
    ADD COLUMN IF NOT EXISTS first_name_kh text,
    ADD COLUMN IF NOT EXISTS last_name_en text,
    ADD COLUMN IF NOT EXISTS last_name_kh text,
    ADD COLUMN IF NOT EXISTS bio_en text,
    ADD COLUMN IF NOT EXISTS bio_kh text,
    ADD COLUMN IF NOT EXISTS address_en text,
    ADD COLUMN IF NOT EXISTS address_kh text;
ALTER TABLE auth_device
    ADD COLUMN IF NOT EXISTS display_name_en varchar(128),
    ADD COLUMN IF NOT EXISTS display_name_kh varchar(128);
ALTER TABLE login_audit
    ADD COLUMN IF NOT EXISTS failure_reason_en text,
    ADD COLUMN IF NOT EXISTS failure_reason_kh text,
    ADD COLUMN IF NOT EXISTS location_en varchar(256),
    ADD COLUMN IF NOT EXISTS location_kh varchar(256),
    ADD COLUMN IF NOT EXISTS country_name_en varchar(128),
    ADD COLUMN IF NOT EXISTS country_name_kh varchar(128),
    ADD COLUMN IF NOT EXISTS city_en varchar(128),
    ADD COLUMN IF NOT EXISTS city_kh varchar(128);
ALTER TABLE security_audit
    ADD COLUMN IF NOT EXISTS description_en text,
    ADD COLUMN IF NOT EXISTS description_kh text;

BEGIN;

INSERT INTO tbl_roles (name, description, created_at, updated_at)
SELECT seed.name, seed.description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('ADMIN', 'Administrator role'),
    ('USER', 'Standard user role'),
    ('HR', 'Human resources role'),
    ('OFFICER', 'Officer role'),
    ('HEAD_OF_DEPARTMENT', 'Department head role')
) AS seed(name, description)
WHERE NOT EXISTS (SELECT 1 FROM tbl_roles role WHERE role.name = seed.name);

INSERT INTO tbl_users
    (username, email, password, enabled, active, role_id, created_at, updated_at)
SELECT
    format('seed100-user-%s', lpad(seed.n::text, 3, '0')),
    format('seed100-user-%s@example.test', lpad(seed.n::text, 3, '0')),
    '!SYNTHETIC-SEED-ACCOUNT-DISABLED!',
    FALSE,
    FALSE,
    (SELECT id FROM tbl_roles WHERE name = 'USER' ORDER BY id LIMIT 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_users existing
    WHERE existing.email = format('seed100-user-%s@example.test', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_user_profiles
    (first_name, last_name, phone_number, avatar_url, bio, address, user_id, birth_date, created_at, updated_at)
SELECT
    format('Synthetic%s', lpad(seed.n::text, 3, '0')),
    format('Seed%s', lpad(seed.n::text, 3, '0')),
    format('+855100%s', lpad(seed.n::text, 5, '0')),
    NULL,
    'Synthetic local development profile; not a real person.',
    format('Development sample address %s', seed.n),
    seed.user_id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT u.id AS user_id,
           row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_user_profiles profile WHERE profile.user_id = seed.user_id
);

INSERT INTO auth_device
    (id, user_id, device_id, display_name, device_type, platform, user_agent,
     last_ip_address, trusted, last_seen_at, revoked_at, created_at, updated_at)
SELECT
    ('00000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid,
    seed.user_id,
    ('10000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid,
    format('Synthetic development device %s', seed.n),
    'TEST_DEVICE',
    'OWSM_SEED',
    'SYNTHETIC-SEED-DATA/1.0',
    '192.0.2.1'::inet,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT u.id AS user_id,
           row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM auth_device existing
    WHERE existing.id = ('00000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid
);

INSERT INTO auth_session
    (session_id, user_id, token_id, created_at, expires_at, revoked_at, ip_address)
SELECT
    ('20000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid,
    seed.user_id,
    ('30000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    '192.0.2.1'::inet
FROM (
    SELECT u.id AS user_id,
           row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM auth_session existing
    WHERE existing.session_id = ('20000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid
);

INSERT INTO login_audit
    (user_id, username, email, ip_address, user_agent, login_status,
     failure_reason, authentication_method, suspicious, mfa_required,
     occurred_at, created_at)
SELECT
    seed.user_id,
    seed.username,
    seed.email,
    '192.0.2.1'::inet,
    'SYNTHETIC-SEED-DATA/1.0',
    'ACCOUNT_DISABLED',
    'Synthetic disabled sample account; not a real login.',
    'SEED',
    FALSE,
    FALSE,
    CURRENT_TIMESTAMP - make_interval(mins => seed.n),
    CURRENT_TIMESTAMP - make_interval(mins => seed.n)
FROM (
    SELECT u.id AS user_id, u.username, u.email,
           row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM login_audit existing
    WHERE existing.email = seed.email
      AND existing.user_agent = 'SYNTHETIC-SEED-DATA/1.0'
);

INSERT INTO security_audit
    (user_id, event_type, event_status, description, occurred_at,
     ip_address, user_agent, device_id, metadata)
SELECT
    seed.user_id,
    'OTHER',
    'SUCCESS',
    format('SYNTHETIC SEED DATA - development-only event %s; not a real security event.', seed.n),
    CURRENT_TIMESTAMP - (seed.n || ' minutes')::interval,
    '192.0.2.1'::inet,
    'SYNTHETIC-SEED-DATA/1.0',
    ('00000000-0000-0000-0000-' || lpad(seed.n::text, 12, '0'))::uuid,
    jsonb_build_object('syntheticSeedData', TRUE, 'environment', 'local-development', 'sequence', seed.n)
FROM (
    SELECT u.id AS user_id,
           row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM security_audit existing
    WHERE existing.description = format(
        'SYNTHETIC SEED DATA - development-only event %s; not a real security event.',
        seed.n
    )
);

UPDATE tbl_roles
SET name_en = CASE name
        WHEN 'ADMIN' THEN 'Administrator'
        WHEN 'USER' THEN 'User'
        WHEN 'HR' THEN 'Human Resources'
        WHEN 'OFFICER' THEN 'Officer'
        WHEN 'HEAD_OF_DEPARTMENT' THEN 'Head of Department'
    END,
    name_kh = CASE name
        WHEN 'ADMIN' THEN 'អ្នកគ្រប់គ្រង'
        WHEN 'USER' THEN 'អ្នកប្រើប្រាស់'
        WHEN 'HR' THEN 'ធនធានមនុស្ស'
        WHEN 'OFFICER' THEN 'មន្ត្រី'
        WHEN 'HEAD_OF_DEPARTMENT' THEN 'ប្រធាននាយកដ្ឋាន'
    END,
    description_en = CASE name
        WHEN 'ADMIN' THEN 'Administrator role'
        WHEN 'USER' THEN 'Standard user role'
        WHEN 'HR' THEN 'Human resources role'
        WHEN 'OFFICER' THEN 'Officer role'
        WHEN 'HEAD_OF_DEPARTMENT' THEN 'Department head role'
    END,
    description_kh = CASE name
        WHEN 'ADMIN' THEN 'តួនាទីអ្នកគ្រប់គ្រង'
        WHEN 'USER' THEN 'តួនាទីអ្នកប្រើប្រាស់ទូទៅ'
        WHEN 'HR' THEN 'តួនាទីផ្នែកធនធានមនុស្ស'
        WHEN 'OFFICER' THEN 'តួនាទីមន្ត្រី'
        WHEN 'HEAD_OF_DEPARTMENT' THEN 'តួនាទីប្រធាននាយកដ្ឋាន'
    END
WHERE name IN ('ADMIN', 'USER', 'HR', 'OFFICER', 'HEAD_OF_DEPARTMENT');

UPDATE tbl_user_profiles profile
SET first_name_en = format('Synthetic%s', lpad(seed.n::text, 3, '0')),
    first_name_kh = format('អ្នកសាកល្បង%s', lpad(seed.n::text, 3, '0')),
    last_name_en = format('Seed%s', lpad(seed.n::text, 3, '0')),
    last_name_kh = format('ទិន្នន័យសាកល្បង%s', lpad(seed.n::text, 3, '0')),
    bio_en = 'Synthetic local development profile; not a real person.',
    bio_kh = 'ប្រវត្តិរូបសាកល្បងសម្រាប់ការអភិវឌ្ឍក្នុងស្រុក មិនមែនជាបុគ្គលពិតទេ។',
    address_en = format('Development sample address %s', seed.n),
    address_kh = format('អាសយដ្ឋានសាកល្បងសម្រាប់ការអភិវឌ្ឍ %s', seed.n)
FROM (
    SELECT u.id AS user_id, row_number() OVER (ORDER BY u.email)::integer AS n
    FROM tbl_users u
    WHERE u.email LIKE 'seed100-user-%@example.test'
) seed
WHERE profile.user_id = seed.user_id;

UPDATE auth_device device
SET display_name_en = format('Synthetic development device %s', seed.n),
    display_name_kh = format('ឧបករណ៍អភិវឌ្ឍន៍សាកល្បង %s', seed.n)
FROM (
    SELECT id, row_number() OVER (ORDER BY id)::integer AS n
    FROM auth_device
    WHERE user_agent = 'SYNTHETIC-SEED-DATA/1.0'
) seed
WHERE device.id = seed.id;

UPDATE login_audit
SET failure_reason_en = 'Synthetic disabled sample account; not a real login.',
    failure_reason_kh = 'គណនីសាកល្បងត្រូវបានបិទ មិនមែនជាការចូលប្រើពិតទេ។',
    location_en = 'Phnom Penh, Cambodia',
    location_kh = 'រាជធានីភ្នំពេញ ប្រទេសកម្ពុជា',
    country_name_en = 'Cambodia',
    country_name_kh = 'កម្ពុជា',
    city_en = 'Phnom Penh',
    city_kh = 'រាជធានីភ្នំពេញ'
WHERE user_agent = 'SYNTHETIC-SEED-DATA/1.0';

UPDATE security_audit
SET description_en = format(
        'SYNTHETIC SEED DATA - development-only event %s; not a real security event.',
        (metadata->>'sequence')::integer
    ),
    description_kh = format(
        'ទិន្នន័យសាកល្បង - ព្រឹត្តិការណ៍សម្រាប់ការអភិវឌ្ឍលេខ %s មិនមែនជាព្រឹត្តិការណ៍សុវត្ថិភាពពិតទេ។',
        (metadata->>'sequence')::integer
    )
WHERE metadata->>'syntheticSeedData' = 'true'
  AND metadata->>'environment' = 'local-development';

COMMIT;
