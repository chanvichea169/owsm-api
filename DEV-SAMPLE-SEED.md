# Local development sample data

These scripts add 100 clearly marked synthetic records to each ordinary
application data table in the local PostgreSQL development stack. They do not
run at application startup and must not be run against staging or production.
The location tables already contain the corrected Cambodia dataset from
`AuthService/src/main/resources/db/seed/CambodiaLocation2025.sql`.

Start the services once so Hibernate creates their tables. From Windows
Command Prompt in the `owsm_api` directory, run:

```cmd
docker compose exec -T postgres psql -U postgres -d auth_db -v ON_ERROR_STOP=1 < AuthService/src/main/resources/db/seed/dev-sample-100.sql
docker compose exec -T postgres psql -U postgres -d news_db -v ON_ERROR_STOP=1 < NewsService/src/main/resources/db/seed/dev-sample-100.sql
docker compose exec -T postgres psql -U postgres -d attendance_db -v ON_ERROR_STOP=1 < attendanceService/src/main/resources/db/seed/dev-sample-100.sql
```

Each script uses a transaction and stable seed markers, so it can be re-run
without duplicating its sample rows. It never truncates or deletes application
data. Sample accounts are disabled/unusable; sample news remains in `DRAFT`;
audit records and attendance notes explicitly identify themselves as
synthetic. Sample media URLs use `.invalid` and do not point to real files.

The seed scripts also add English and Khmer columns for user-facing text in
the Auth, News, and Attendance databases and populate both languages for all
100 sample rows. Location tables already have their own Khmer/English columns.
Machine identifiers, email addresses, enums, join tables, and session/token
fields remain unchanged because they are not display text.

`tbl_roles` has only five legal, unique role values, so the auth script ensures
those roles exist rather than inserting 100 duplicate roles. Real province,
district, commune, and village reference data is left unchanged. Flyway
metadata and the empty gateway database are intentionally excluded.

## Verify sample rows

```sql
-- auth_db: expected 100 matching seed rows in each of these six tables.
SELECT COUNT(*) FROM tbl_users WHERE email LIKE 'seed100-user-%@example.test';
SELECT COUNT(*) FROM tbl_user_profiles p JOIN tbl_users u ON u.id = p.user_id
WHERE u.email LIKE 'seed100-user-%@example.test';
SELECT COUNT(*) FROM auth_device d JOIN tbl_users u ON u.id = d.user_id
WHERE u.email LIKE 'seed100-user-%@example.test';
SELECT COUNT(*) FROM auth_session s JOIN tbl_users u ON u.id = s.user_id
WHERE u.email LIKE 'seed100-user-%@example.test';
SELECT COUNT(*) FROM login_audit WHERE user_agent = 'SYNTHETIC-SEED-DATA/1.0';
SELECT COUNT(*) FROM security_audit WHERE description LIKE 'SYNTHETIC SEED DATA - development-only event %';

-- news_db: each result should be 100.
SELECT COUNT(*) FROM tbl_authors WHERE email LIKE 'seed100-author-%@example.test';
SELECT COUNT(*) FROM tbl_categories WHERE slug LIKE 'seed100-category-%';
SELECT COUNT(*) FROM tbl_tags WHERE slug LIKE 'seed100-tag-%';
SELECT COUNT(*) FROM tbl_news WHERE slug LIKE 'seed100-news-%';
SELECT COUNT(*) FROM tbl_news_tags nt JOIN tbl_news n ON n.id = nt.news_id WHERE n.slug LIKE 'seed100-news-%';
SELECT COUNT(*) FROM tbl_news_images ni JOIN tbl_news n ON n.id = ni.news_id WHERE n.slug LIKE 'seed100-news-%';
SELECT COUNT(*) FROM tbl_comments WHERE content LIKE 'SYNTHETIC SEED COMMENT %';
SELECT COUNT(*) FROM tbl_media_assets WHERE original_file_name LIKE 'seed100-media-%';

-- attendance_db: each result should be 100.
SELECT COUNT(*) FROM companies WHERE code LIKE 'SEED100-COMPANY-%';
SELECT COUNT(*) FROM offices WHERE code LIKE 'SEED100-OFFICE-%';
SELECT COUNT(*) FROM employees WHERE username LIKE 'seed100-employee-%';
SELECT COUNT(*) FROM attendance_records WHERE notes LIKE 'SYNTHETIC SEED DATA - local development record %';
```
