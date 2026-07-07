CREATE UNIQUE INDEX IF NOT EXISTS ux_banner_display_order_active
    ON banner(display_order)
    WHERE is_active = true;

INSERT INTO banner (image_url, display_order, is_active) VALUES
('https://.../banner1.png', 1, true),
('https://.../banner2.png', 2, true),
('https://.../banner3.png', 3, true),
('https://.../banner4.png', 4, true),
('https://.../banner5.png', 5, true);