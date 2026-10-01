# Cambodia location database seed

`src/main/resources/db/seed/CambodiaLocation2025.sql` loads the Cambodia
location workbook into the PostgreSQL tables used by AuthService:
`tbl_provinces`, `tbl_districts`, `tbl_communes`, and `tbl_villages`.

## Import

Start AuthService once before importing so Hibernate has created the entity
tables. From the `owsm_api` directory, run this in Windows Command Prompt:

```cmd
docker compose exec -T postgres psql -U postgres -d auth_db -v ON_ERROR_STOP=1 < AuthService/src/main/resources/db/seed/CambodiaLocation2025.sql
```

The script is transactional and uses `ON CONFLICT` upserts, so it can be
re-run safely. It does not delete other database rows. It updates matching
location codes to the values in the workbook.

## Source corrections

The corrected workbook is saved separately as
`CambodiaLocation2025-Corrected.xlsx`; the original workbook is unchanged.
The seed imports 25 provinces, 210 districts, 1,662 communes, and 14,576
villages.

- 81 village codes were repaired to be unique and follow the
  `commune_code + two-digit village sequence` format.
- The workbook has 13 villages for commune `120913`, but no row for that
  commune. To preserve all villages and satisfy the database foreign key, the
  seed adds commune `120913` under province `12` and district `1209`, with
  explicit placeholder names indicating that the source did not provide its
  name. Replace those names with the official names when available.

The repaired village identifiers are database identifiers; if existing users
are already linked to one of the affected village codes, review those links
before importing so user addresses are not reassigned.

## Verify

Run these checks after importing; expected counts are shown in comments:

```sql
SELECT 'provinces' AS location, COUNT(*) FROM tbl_provinces
UNION ALL SELECT 'districts', COUNT(*) FROM tbl_districts
UNION ALL SELECT 'communes', COUNT(*) FROM tbl_communes
UNION ALL SELECT 'villages', COUNT(*) FROM tbl_villages;
-- Expected: 25, 210, 1662, 14576 (existing unrelated rows may increase totals).

SELECT COUNT(*) AS districts_without_province
FROM tbl_districts d LEFT JOIN tbl_provinces p USING (province_code)
WHERE p.province_code IS NULL;

SELECT COUNT(*) AS communes_without_district
FROM tbl_communes c LEFT JOIN tbl_districts d USING (district_code)
WHERE d.district_code IS NULL;

SELECT COUNT(*) AS villages_without_commune
FROM tbl_villages v LEFT JOIN tbl_communes c USING (commune_code)
WHERE c.commune_code IS NULL;
-- Each orphan count should be 0.
```

AuthService caches location API results in Redis for five minutes. The
previously cached empty province list was cleared after this import; any other
stale location responses expire within five minutes. After future imports,
wait for that cache TTL before checking locations in the UI.
