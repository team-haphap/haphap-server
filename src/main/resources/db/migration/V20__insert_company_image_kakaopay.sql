INSERT INTO company_image (company_id, type, image_url, created_at, updated_at)

-- 카카오페이
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3ab48056-e581-46c7-a5b7-844a66fe4838-home_kakaopay.png', NOW(), NOW()
FROM company WHERE name = '카카오페이'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/f1e14729-8fbd-4652-b610-ac6597f72f5d-list_kakaopay.png', NOW(), NOW()
FROM company WHERE name = '카카오페이'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/20285d23-0b68-47fb-9b72-980e7a45389b-detail_kakaopay.png', NOW(), NOW()
FROM company WHERE name = '카카오페이'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/90a5f51c-4d5e-4798-89b8-4f6c80a73178-logo_home.png', NOW(), NOW()
FROM company WHERE name = '카카오페이'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/be3efab3-97dc-433a-80ac-9b00c3ce1657-logo_calender.png', NOW(), NOW()
FROM company WHERE name = '카카오페이';