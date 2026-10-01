-- Local development sample data only. URLs use the reserved .invalid domain.
-- Re-runnable: each entity row uses a stable seed marker.
ALTER TABLE tbl_authors
    ADD COLUMN IF NOT EXISTS full_name_en text,
    ADD COLUMN IF NOT EXISTS full_name_kh text,
    ADD COLUMN IF NOT EXISTS bio_en text,
    ADD COLUMN IF NOT EXISTS bio_kh text;
ALTER TABLE tbl_categories
    ADD COLUMN IF NOT EXISTS name_en text,
    ADD COLUMN IF NOT EXISTS name_kh text,
    ADD COLUMN IF NOT EXISTS description_en text,
    ADD COLUMN IF NOT EXISTS description_kh text;
ALTER TABLE tbl_tags
    ADD COLUMN IF NOT EXISTS name_en text,
    ADD COLUMN IF NOT EXISTS name_kh text;
ALTER TABLE tbl_news
    ADD COLUMN IF NOT EXISTS title_en text,
    ADD COLUMN IF NOT EXISTS title_kh text,
    ADD COLUMN IF NOT EXISTS content_en text,
    ADD COLUMN IF NOT EXISTS content_kh text;
ALTER TABLE tbl_comments
    ADD COLUMN IF NOT EXISTS user_name_en text,
    ADD COLUMN IF NOT EXISTS user_name_kh text,
    ADD COLUMN IF NOT EXISTS content_en text,
    ADD COLUMN IF NOT EXISTS content_kh text;
ALTER TABLE tbl_media_assets
    ADD COLUMN IF NOT EXISTS category_en text,
    ADD COLUMN IF NOT EXISTS category_kh text;

BEGIN;

INSERT INTO tbl_authors (full_name, email, bio, avatar_url, is_active, created_at)
SELECT
    format('Synthetic Seed Author %s', lpad(seed.n::text, 3, '0')),
    format('seed100-author-%s@example.test', lpad(seed.n::text, 3, '0')),
    'Synthetic local development author; not a real person.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_authors existing
    WHERE existing.email = format('seed100-author-%s@example.test', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_categories (name, slug, description, parent_id, created_at, updated_at)
SELECT
    format('Synthetic Seed Category %s', lpad(seed.n::text, 3, '0')),
    format('seed100-category-%s', lpad(seed.n::text, 3, '0')),
    'Synthetic local development category.',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_categories existing
    WHERE existing.slug = format('seed100-category-%s', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_tags (name, slug, created_at)
SELECT
    format('Synthetic Seed Tag %s', lpad(seed.n::text, 3, '0')),
    format('seed100-tag-%s', lpad(seed.n::text, 3, '0')),
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_tags existing
    WHERE existing.slug = format('seed100-tag-%s', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_news
    (title, slug, content, cover_image, status, is_featured, view_count,
     published_at, author_id, category_id, created_at, updated_at)
SELECT
    format('Synthetic Seed News %s', lpad(seed.n::text, 3, '0')),
    format('seed100-news-%s', lpad(seed.n::text, 3, '0')),
    format('Synthetic local development news content %s. This is sample data, not a real news report.', seed.n),
    format('https://seed-%s.invalid/cover.jpg', lpad(seed.n::text, 3, '0')),
    'DRAFT',
    FALSE,
    0,
    NULL,
    author.id,
    category.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN tbl_authors author
  ON author.email = format('seed100-author-%s@example.test', lpad(seed.n::text, 3, '0'))
JOIN tbl_categories category
  ON category.slug = format('seed100-category-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_news existing
    WHERE existing.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_news_tags (news_id, tag_id)
SELECT news.id, tag.id
FROM generate_series(1, 100) AS seed(n)
JOIN tbl_news news
  ON news.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'))
JOIN tbl_tags tag
  ON tag.slug = format('seed100-tag-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_news_tags existing
    WHERE existing.news_id = news.id AND existing.tag_id = tag.id
);

INSERT INTO tbl_news_images (news_id, image_path)
SELECT news.id, format('https://seed-%s.invalid/image.jpg', lpad(seed.n::text, 3, '0'))
FROM generate_series(1, 100) AS seed(n)
JOIN tbl_news news
  ON news.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_news_images existing
    WHERE existing.news_id = news.id
      AND existing.image_path = format('https://seed-%s.invalid/image.jpg', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_comments
    (news_id, user_name, user_email, content, is_approved, created_at, updated_at)
SELECT
    news.id,
    format('Synthetic Commenter %s', lpad(seed.n::text, 3, '0')),
    format('seed100-commenter-%s@example.test', lpad(seed.n::text, 3, '0')),
    format('SYNTHETIC SEED COMMENT %s - local development only.', lpad(seed.n::text, 3, '0')),
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN tbl_news news
  ON news.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_comments existing
    WHERE existing.content = format('SYNTHETIC SEED COMMENT %s - local development only.', lpad(seed.n::text, 3, '0'))
);

INSERT INTO tbl_media_assets
    (news_id, file_url, file_type, file_size, category,
     original_file_name, stored_file_name, created_at, updated_at)
SELECT
    news.id,
    format('https://seed-%s.invalid/media.jpg', lpad(seed.n::text, 3, '0')),
    'image/jpeg',
    1024,
    'SYNTHETIC_SEED_DATA',
    format('seed100-media-%s.jpg', lpad(seed.n::text, 3, '0')),
    format('seed100-media-%s.jpg', lpad(seed.n::text, 3, '0')),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 100) AS seed(n)
JOIN tbl_news news
  ON news.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_media_assets existing
    WHERE existing.original_file_name = format('seed100-media-%s.jpg', lpad(seed.n::text, 3, '0'))
);

UPDATE tbl_authors author
SET full_name_en = format('Synthetic Seed Author %s', lpad(seed.n::text, 3, '0')),
    full_name_kh = format('អ្នកនិពន្ធសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    bio_en = 'Synthetic local development author; not a real person.',
    bio_kh = 'អ្នកនិពន្ធសាកល្បងសម្រាប់ការអភិវឌ្ឍក្នុងស្រុក មិនមែនជាបុគ្គលពិតទេ។'
FROM generate_series(1, 100) AS seed(n)
WHERE author.email = format('seed100-author-%s@example.test', lpad(seed.n::text, 3, '0'));

UPDATE tbl_categories category
SET name_en = format('Synthetic Seed Category %s', lpad(seed.n::text, 3, '0')),
    name_kh = format('ប្រភេទសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    description_en = 'Synthetic local development category.',
    description_kh = 'ប្រភេទទិន្នន័យសាកល្បងសម្រាប់ការអភិវឌ្ឍក្នុងស្រុក។'
FROM generate_series(1, 100) AS seed(n)
WHERE category.slug = format('seed100-category-%s', lpad(seed.n::text, 3, '0'));

UPDATE tbl_tags tag
SET name_en = format('Synthetic Seed Tag %s', lpad(seed.n::text, 3, '0')),
    name_kh = format('ស្លាកសាកល្បង %s', lpad(seed.n::text, 3, '0'))
FROM generate_series(1, 100) AS seed(n)
WHERE tag.slug = format('seed100-tag-%s', lpad(seed.n::text, 3, '0'));

UPDATE tbl_news news
SET title_en = format('Synthetic Seed News %s', lpad(seed.n::text, 3, '0')),
    title_kh = format('ព័ត៌មានសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    content_en = format('Synthetic local development news content %s. This is sample data, not a real news report.', seed.n),
    content_kh = format('ខ្លឹមសារព័ត៌មានសាកល្បងសម្រាប់ការអភិវឌ្ឍក្នុងស្រុកលេខ %s។ នេះជាទិន្នន័យគំរូ មិនមែនជាព័ត៌មានពិតទេ។', seed.n)
FROM generate_series(1, 100) AS seed(n)
WHERE news.slug = format('seed100-news-%s', lpad(seed.n::text, 3, '0'));

UPDATE tbl_comments comment
SET user_name_en = format('Synthetic Commenter %s', lpad(seed.n::text, 3, '0')),
    user_name_kh = format('អ្នកបញ្ចេញមតិសាកល្បង %s', lpad(seed.n::text, 3, '0')),
    content_en = format('SYNTHETIC SEED COMMENT %s - local development only.', lpad(seed.n::text, 3, '0')),
    content_kh = format('មតិសាកល្បងលេខ %s សម្រាប់ការអភិវឌ្ឍក្នុងស្រុកប៉ុណ្ណោះ។', lpad(seed.n::text, 3, '0'))
FROM generate_series(1, 100) AS seed(n)
WHERE comment.content = format('SYNTHETIC SEED COMMENT %s - local development only.', lpad(seed.n::text, 3, '0'));

UPDATE tbl_media_assets media
SET category_en = 'Synthetic development media',
    category_kh = 'មេឌៀសាកល្បងសម្រាប់ការអភិវឌ្ឍ'
FROM generate_series(1, 100) AS seed(n)
WHERE media.original_file_name = format('seed100-media-%s.jpg', lpad(seed.n::text, 3, '0'));

COMMIT;
