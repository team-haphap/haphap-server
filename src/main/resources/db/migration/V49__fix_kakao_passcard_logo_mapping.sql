UPDATE company
SET card_logo_image_url = CASE id
                              WHEN 2 THEN 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/8c4075b1-5708-4d3f-bad5-6f9f954ece45-passcard-logo-kakaobank.webp'
                              WHEN 7 THEN 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/9c9ee713-404d-439b-834c-9a48dacf9e1a-passcard-logo-kakao.webp'
                              ELSE card_logo_image_url
    END,
    updated_at = NOW()
WHERE id IN (2, 7);