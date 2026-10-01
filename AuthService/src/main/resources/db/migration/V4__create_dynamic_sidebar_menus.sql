CREATE TABLE IF NOT EXISTS tbl_sidebar_menus (
    menu_key VARCHAR(64) PRIMARY KEY,
    label_en VARCHAR(120) NOT NULL,
    label_km VARCHAR(120),
    route_path VARCHAR(255),
    icon_name VARCHAR(32) NOT NULL,
    parent_key VARCHAR(64)
        REFERENCES tbl_sidebar_menus (menu_key) ON DELETE RESTRICT,
    sort_order INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tbl_sidebar_menus
    (menu_key, label_en, route_path, icon_name, parent_key, sort_order, enabled)
VALUES
    ('dashboard', 'app.nav.dashboard', '/', 'dashboard', NULL, 10, TRUE),
    ('news-management', 'app.nav.news-management', NULL, 'news', NULL, 20, TRUE),
    ('news-list', 'app.nav.news-list', '/news', 'list', 'news-management', 21, TRUE),
    ('categories', 'app.nav.categories', '/news/categories', 'folder', 'news-management', 22, TRUE),
    ('media-videos', 'app.nav.media-videos', '/news/media', 'video', 'news-management', 23, TRUE),
    ('audio', 'app.nav.audio', '/news/audio', 'music', 'news-management', 24, TRUE),
    ('staff-management', 'app.nav.staff-management', NULL, 'users', NULL, 30, TRUE),
    ('attendance', 'app.nav.attendance', '/attendance', 'calendar', 'staff-management', 31, TRUE),
    ('employee', 'app.nav.employee', '/attendance/employees', 'users', 'staff-management', 32, TRUE),
    ('department', 'app.nav.department', '/attendance/departments', 'building', 'staff-management', 33, TRUE),
    ('offices', 'app.nav.offices', '/attendance/offices', 'map-pin', 'staff-management', 34, TRUE),
    ('attendance-report', 'app.nav.attendance-report', '/attendance/report', 'reports', 'staff-management', 35, TRUE),
    ('reports', 'app.nav.reports', '/reports', 'reports', NULL, 40, TRUE),
    ('security-audit', 'app.nav.security-audit', '/security/audit', 'shield', NULL, 50, TRUE),
    ('settings', 'app.nav.settings', '/settings', 'settings', NULL, 60, TRUE)
ON CONFLICT (menu_key) DO NOTHING;
