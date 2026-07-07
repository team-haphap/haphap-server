CREATE TABLE IF NOT EXISTS banner (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    main_message VARCHAR(200) NOT NULL,
    sub_message VARCHAR(200) NOT NULL,
    display_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_banner_display_order ON banner(display_order);

-- 활성 배너 사이에서는 display_order가 겹치면 안 되므로 부분 유니크 인덱스로 강제
CREATE UNIQUE INDEX IF NOT EXISTS ux_banner_display_order_active
    ON banner(display_order)
    WHERE is_active = true;

INSERT INTO banner (image_url, main_message, sub_message, display_order, is_active) VALUES
('https://.../banner1.png', '지원 이후, 보이지 않던 기다림을 더욱 선명하게', '같은 공고 지원자들의 결과를 확인해보세요!', 1, true),
('https://.../banner2.png', '메인 메세지 예시 2', '서브 메세지 예시 2', 2, true),
('https://.../banner3.png', '메인 메세지 예시 3', '서브 메세지 예시 3', 3, true),
('https://.../banner4.png', '메인 메세지 예시 4', '서브 메세지 예시 4', 4, true),
('https://.../banner5.png', '메인 메세지 예시 5', '서브 메세지 예시 5', 5, true);