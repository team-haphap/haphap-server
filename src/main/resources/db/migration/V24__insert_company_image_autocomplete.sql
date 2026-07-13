INSERT INTO company_image (company_id, type, image_url, created_at, updated_at)

SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/e4ebe4b5-6d36-4a25-9702-fea943933b3b-auto_logo_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/fe14c326-6fb6-433c-b6b1-ce07698837e7-auto_logo_kakaopay.png', NOW(), NOW()
FROM company WHERE name = '카카오페이'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/bc98da26-d68a-4da8-8457-fb1d33f42532-auto_logo_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/5c346345-e579-483a-a2ff-bebd420f5435-auto_logo_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/07ae7abf-f9ca-4de2-bd31-e6e96d9c1b85-auto_logo_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/84e264e9-345d-45c1-be9d-ed6bce256992-auto_logo_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'
UNION ALL
SELECT id, 'AUTOCOMPLETE', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/30874443-fd88-411b-acf4-8b4984f5c471-auto_logo_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG';