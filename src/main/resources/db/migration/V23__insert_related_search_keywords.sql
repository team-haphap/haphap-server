INSERT INTO related_search_keyword (keyword, is_active, created_at, updated_at)
VALUES
    ('카카오', true, NOW(), NOW()),
    ('카카오 스타일', true, NOW(), NOW()),
    ('카카오 뱅크', true, NOW(), NOW()),
    ('카카오 맵', true, NOW(), NOW())
ON CONFLICT (keyword) DO NOTHING;