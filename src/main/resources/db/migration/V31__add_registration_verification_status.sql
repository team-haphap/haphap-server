-- 합격 인증(인증샷 검토) 상태 컬럼 추가. 실제 승인/반려 플로우는 별도 파트에서 붙일 예정이고,
-- 여기서는 전형 이동 신정책(다음 전형 합격 인증 1건 승인 시 이동)이 참조할 상태값만 채워둔다.
ALTER TABLE registration
    ADD COLUMN verification_status VARCHAR(20);

UPDATE registration
SET verification_status = CASE
    WHEN result = 'PASS' THEN 'PENDING'
    ELSE 'NOT_REQUIRED'
END;

ALTER TABLE registration
    ALTER COLUMN verification_status SET NOT NULL;
