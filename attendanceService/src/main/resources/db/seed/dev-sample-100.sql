-- Local development sample data only. Employee passwords are unusable markers.
-- Re-runnable: rows are guarded by stable codes, usernames, emails, or notes.
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS name_en text,
    ADD COLUMN IF NOT EXISTS name_kh text,
    ADD COLUMN IF NOT EXISTS address_en text,
    ADD COLUMN IF NOT EXISTS address_kh text;
ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS name_en text,
    ADD COLUMN IF NOT EXISTS name_kh text,
    ADD COLUMN IF NOT EXISTS address_en text,
    ADD COLUMN IF NOT EXISTS address_kh text;
ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS first_name_en text,
    ADD COLUMN IF NOT EXISTS first_name_kh text,
    ADD COLUMN IF NOT EXISTS last_name_en text,
    ADD COLUMN IF NOT EXISTS last_name_kh text;
ALTER TABLE attendance_records
    ADD COLUMN IF NOT EXISTS notes_en text,
    ADD COLUMN IF NOT EXISTS notes_kh text;

BEGIN;

INSERT INTO companies (name, code, address, email, phone_number, created_at, updated_at)
SELECT
    format('Synthetic Seed Company %s', lpad(seed.n::text, 3, '0')),
    format('SEED100-COMPANY-%s', lpad(seed.n::text, 3, '0')),
    format('Development-only sample address %s', seed.n),
    format('seed100-company-%s@example.test', lpad(seed.n::text, 3, '0')),
    format('+855200%s', lpad(seed.n::text, 5, '0')),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
WHERE NOT EXISTS (
    SELECT 1 FROM companies existing
    WHERE existing.code = format('SEED100-COMPANY-%s', lpad(seed.n::text, 3, '0'))
);

INSERT INTO offices
    (name, code, address, email, phone_number, company_id, created_at, updated_at)
SELECT
    format('Synthetic Seed Office %s', lpad(seed.n::text, 3, '0')),
    format('SEED100-OFFICE-%s', lpad(seed.n::text, 3, '0')),
    format('Development-only office address %s', seed.n),
    format('seed100-office-%s@example.test', lpad(seed.n::text, 3, '0')),
    format('+855300%s', lpad(seed.n::text, 5, '0')),
    company.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN companies company
  ON company.code = format('SEED100-COMPANY-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM offices existing
    WHERE existing.code = format('SEED100-OFFICE-%s', lpad(seed.n::text, 3, '0'))
);

INSERT INTO employees
    (first_name, last_name, email, username, password, phone_number,
     requires_password_change, company_id, office_id, created_at, updated_at)
SELECT
    format('Synthetic%s', lpad(seed.n::text, 3, '0')),
    format('Seed%s', lpad(seed.n::text, 3, '0')),
    format('seed100-employee-%s@example.test', lpad(seed.n::text, 3, '0')),
    format('seed100-employee-%s', lpad(seed.n::text, 3, '0')),
    '!SYNTHETIC-SEED-ONLY-NOT-A-LOGIN-PASSWORD!',
    format('+855400%s', lpad(seed.n::text, 5, '0')),
    TRUE,
    company.id,
    office.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN companies company
  ON company.code = format('SEED100-COMPANY-%s', lpad(seed.n::text, 3, '0'))
JOIN offices office
  ON office.code = format('SEED100-OFFICE-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM employees existing
    WHERE existing.email = format('seed100-employee-%s@example.test', lpad(seed.n::text, 3, '0'))
);

INSERT INTO attendance_records
    (employee_id, type, recorded_at, latitude, longitude, notes, created_at, updated_at)
SELECT
    employee.id,
    CASE (seed.n - 1) % 4
        WHEN 0 THEN 'CHECK_IN'
        WHEN 1 THEN 'CHECK_OUT'
        WHEN 2 THEN 'BREAK_START'
        ELSE 'BREAK_END'
    END,
    CURRENT_TIMESTAMP - make_interval(mins => seed.n),
    11.5564,
    104.9282,
    format('SYNTHETIC SEED DATA - local development record %s.', lpad(seed.n::text, 3, '0')),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN employees employee
  ON employee.username = format('seed100-employee-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_records existing
    WHERE existing.notes = format('SYNTHETIC SEED DATA - local development record %s.', lpad(seed.n::text, 3, '0'))
);

UPDATE companies company
SET name_en = format('Synthetic Seed Company %s', lpad(seed.n::text, 3, '0')),
    name_kh = format('ក្រុមហ៊ុនសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    address_en = format('Development-only sample address %s', seed.n),
    address_kh = format('អាសយដ្ឋានសាកល្បងសម្រាប់ការអភិវឌ្ឍ %s', seed.n)
FROM generate_series(1, 100) AS seed(n)
WHERE company.code = format('SEED100-COMPANY-%s', lpad(seed.n::text, 3, '0'));

UPDATE offices office
SET name_en = format('Synthetic Seed Office %s', lpad(seed.n::text, 3, '0')),
    name_kh = format('ការិយាល័យសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    address_en = format('Development-only office address %s', seed.n),
    address_kh = format('អាសយដ្ឋានការិយាល័យសាកល្បង %s', seed.n)
FROM generate_series(1, 100) AS seed(n)
WHERE office.code = format('SEED100-OFFICE-%s', lpad(seed.n::text, 3, '0'));

UPDATE employees employee
SET first_name_en = format('Synthetic%s', lpad(seed.n::text, 3, '0')),
    first_name_kh = format('បុគ្គលិកសាកល្បង%s', lpad(seed.n::text, 3, '0')),
    last_name_en = format('Seed%s', lpad(seed.n::text, 3, '0')),
    last_name_kh = format('ទិន្នន័យសាកល្បង%s', lpad(seed.n::text, 3, '0'))
FROM generate_series(1, 100) AS seed(n)
WHERE employee.username = format('seed100-employee-%s', lpad(seed.n::text, 3, '0'));

UPDATE attendance_records record
SET notes_en = record.notes,
    notes_kh = format('ទិន្នន័យវត្តមានសាកល្បងលេខ %s សម្រាប់ការអភិវឌ្ឍក្នុងស្រុក។', lpad(seed.n::text, 3, '0'))
FROM generate_series(1, 100) AS seed(n)
WHERE record.notes = format('SYNTHETIC SEED DATA - local development record %s.', lpad(seed.n::text, 3, '0'));

COMMIT;
