INSERT INTO company_image (company_id, type, image_url, created_at, updated_at)

-- 토스
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/8c860bbc-478d-4219-8220-2e3517bdbd83-home_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/a69f9d85-da1a-4be3-b1ad-95746819bf0a-list_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/31395569-4ccc-484f-aa06-addabac8ba9a-detail_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/aa0ecc80-efe2-4b9a-9db8-3634d407ab47-home_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/f470a42a-649b-4408-a845-4270ba7434c0-calender_toss.png', NOW(), NOW()
FROM company WHERE name = '토스'

-- 카카오
UNION ALL
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/2ac9c2ed-f1f4-46b8-a19f-dd78d965f454-home_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/73d28707-e13d-46da-bf87-a6dce00c372b-list_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/aa53dfbe-1dbb-4307-acae-51b755b043a7-detail_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/b93d8d83-56fc-4138-8e19-c66cd5b17b1d-home_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/aac7b3d6-42cb-4499-be3a-e0915d9c5e04-calender_kakao.png', NOW(), NOW()
FROM company WHERE name = '카카오'

-- 네이버
UNION ALL
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3b81705a-f129-4522-afe9-42f928541e96-home_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/4c8ca56e-59a9-4c94-b2ef-bafcbf3f648e-list_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/90734025-1a97-4151-945e-b33cdb31cfaf-detail_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/7233e784-00b4-4144-b6ea-e2685351959f-home_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/72d81b59-15fc-479d-bf83-8babdc5fa12b-calender_naver.png', NOW(), NOW()
FROM company WHERE name = '네이버'

-- 아모레퍼시픽
UNION ALL
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/f36c4c11-10eb-4511-8370-a54e696629c6-home_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3116c871-7ce3-4a76-b351-b4dd7f60212d-list_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5f38a277-9f4f-4c51-ad41-3d4c83bdcfe7-detail_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/2d3e6ab0-11f0-4891-9971-c4685603a535-home_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/82d758eb-1c80-4361-b060-62b9bdc89bdc-calender_amore.png', NOW(), NOW()
FROM company WHERE name = '아모레퍼시픽'

-- 현대자동차
UNION ALL
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5855cf81-0132-4566-adba-bb1ddb97378c-home_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/6ac1f400-3d6a-4e51-b4a8-ba43abdbc576-list_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/82b19c59-c8df-456c-b78a-d0f74e9dffdc-detail_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/6324da13-ac26-449d-9be9-2bb9c23f55bc-home_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/218a8bd2-dd0f-41e2-a047-13d4d3d551aa-calender_hyundai.png', NOW(), NOW()
FROM company WHERE name = '현대자동차'

-- LG
UNION ALL
SELECT id, 'POPULAR', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5f49eca0-59c9-4f26-a9b3-e8790a8f1606-home_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG생활건강'
UNION ALL
SELECT id, 'LISTING', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/80fc1ddf-6f99-4f9b-bd7b-cd33e51d38b1-list_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG생활건강'
UNION ALL
SELECT id, 'DETAIL', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3fad0aab-3de5-47a8-80b7-14c6b782dfa0-detail_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG생활건강'
UNION ALL
SELECT id, 'TODAY_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/25035d03-1558-4c9b-adf7-5615d9d1f6af-home_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG생활건강'
UNION ALL
SELECT id, 'CALENDAR_LOGO', 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/01bbc81d-eba3-454d-af5d-e11fa899452a-calender_lg.png', NOW(), NOW()
FROM company WHERE name = 'LG생활건강';