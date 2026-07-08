-- registration.stage(varchar)는 stage_id(FK)로 대체됨.
-- 엔티티에 대응 필드가 없어 validate 에러를 유발하므로 제거.
ALTER TABLE registration DROP COLUMN stage;