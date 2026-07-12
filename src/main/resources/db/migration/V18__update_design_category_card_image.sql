UPDATE category
SET card_image_url = 'https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/d89ad71d-eeb8-43e9-8197-576c506bfa64-pass_design.png',
    updated_at = NOW()
WHERE name = '디자인';