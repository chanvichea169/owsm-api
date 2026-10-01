CREATE TABLE IF NOT EXISTS tbl_role_menu_configurations (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tbl_role_menu_items (
    configuration_id BIGINT NOT NULL
        REFERENCES tbl_role_menu_configurations (id) ON DELETE CASCADE,
    menu_key VARCHAR(64) NOT NULL,
    PRIMARY KEY (configuration_id, menu_key)
);
