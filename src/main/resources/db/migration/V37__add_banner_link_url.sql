-- 홈 배너 인스타그램 연동용 링크. null이면 클릭 동작 없는 디자인 배너.
ALTER TABLE banner ADD COLUMN link_url VARCHAR(500);