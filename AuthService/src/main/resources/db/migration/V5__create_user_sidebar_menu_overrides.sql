CREATE TABLE IF NOT EXISTS tbl_user_menu_configurations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE
        REFERENCES tbl_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tbl_user_menu_items (
    configuration_id BIGINT NOT NULL
        REFERENCES tbl_user_menu_configurations (id) ON DELETE CASCADE,
    menu_key VARCHAR(64) NOT NULL
        REFERENCES tbl_sidebar_menus (menu_key) ON DELETE CASCADE,
    PRIMARY KEY (configuration_id, menu_key)
);
