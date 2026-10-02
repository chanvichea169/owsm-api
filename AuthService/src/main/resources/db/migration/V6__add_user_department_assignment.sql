ALTER TABLE tbl_users
    ADD COLUMN IF NOT EXISTS department_id BIGINT;
