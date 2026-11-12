-- current_stage_id 백필.
-- V30에서 컬럼만 추가되고, 첫 전형 등록 시 초기화(AdminPostingStageService)는 이후에 생겨서
-- 그 전에 등록된 공고·시드 공고는 current_stage_id가 NULL이다.
-- NULL인 동안 들어온 합격 승인은 StageTransitionAdvancer가 무시했으므로, 같은 규칙으로 다시 계산한다.
--
-- 규칙 (StageTransitionAdvancer와 동일):
--   1) 시작은 그 공고의 가장 앞 전형
--   2) 현재 전형 "자신의" 합격 승인이 있고 바로 다음 순서(order_index + 1) 전형이 있으면 그 전형으로 이동
--      - 현재 전형에 도착한 뒤의 승인만 인정 (도착 전 승인은 실시간이었어도 무시됐을 것)
--      - 이전/후속 전형 승인으로는 건너뛰지 않는다 → 운영진 수동 이동 대상으로 남김
--   3) 최종합격(FINAL_PASS)은 다음이 없으므로 멈추고, 자신의 승인이 있으면 마지막 승인 시각을 moved_at으로
--      (Posting.isClosed()가 moved_at + 4일 기준으로 마감을 판단)
--
-- moved_at: 이동해 온 전형은 직전 전형의 첫 승인 시각, 처음 전형은 백필 시각(서비스의 초기화와 동일).
-- 승인 시각은 approve() 시점에 갱신되는 registration.updated_at으로 본다.
-- 특정 공고 id를 쓰지 않으므로 DB(로컬/운영)마다 각자의 데이터로 계산된다. 이미 값이 있는 공고는 건드리지 않는다.
-- 알림은 발송하지 않는다 (SQL이라 이벤트가 없음).

CREATE TEMPORARY TABLE tmp_current_stage_backfill AS
WITH RECURSIVE
chain AS (
    -- 1) 가장 앞 전형
    SELECT f.posting_id, f.id AS stage_id, f.order_index, f.stage_type,
           CAST(NULL AS TIMESTAMP) AS arrived_at, 1 AS depth
    FROM (
        SELECT DISTINCT ON (s.posting_id) s.posting_id, s.id, s.order_index, s.stage_type
        FROM posting_stage s
        JOIN posting p ON p.id = s.posting_id
        WHERE p.current_stage_id IS NULL
        ORDER BY s.posting_id, s.order_index
    ) f

    UNION ALL

    -- 2) 현재 전형 자신의 (도착 이후) 첫 승인이 있으면 다음 순서 전형으로
    SELECT c.posting_id, nxt.id, nxt.order_index, nxt.stage_type, a.approved_at, c.depth + 1
    FROM chain c
    JOIN LATERAL (
        SELECT r.updated_at AS approved_at
        FROM registration r
        WHERE r.stage_id = c.stage_id
          AND r.result = 'PASS'
          AND r.verification_status = 'APPROVED'
          AND (c.arrived_at IS NULL OR r.updated_at >= c.arrived_at)
        ORDER BY r.updated_at
        LIMIT 1
    ) a ON TRUE
    JOIN posting_stage nxt
      ON nxt.posting_id = c.posting_id
     AND nxt.order_index = c.order_index + 1
    WHERE c.stage_type IS DISTINCT FROM 'FINAL_PASS'
),
final_stage AS (
    SELECT DISTINCT ON (posting_id) posting_id, stage_id, stage_type, arrived_at
    FROM chain
    ORDER BY posting_id, depth DESC
)
SELECT fs.posting_id,
       fs.stage_id,
       COALESCE(
           -- 3) 최종합격 자신의 승인이 있으면 마지막 승인 시각 (confirmFinalPass와 동일)
           CASE WHEN fs.stage_type = 'FINAL_PASS' THEN (
               SELECT MAX(r.updated_at)
               FROM registration r
               WHERE r.stage_id = fs.stage_id
                 AND r.result = 'PASS'
                 AND r.verification_status = 'APPROVED'
                 AND (fs.arrived_at IS NULL OR r.updated_at >= fs.arrived_at)
           ) END,
           fs.arrived_at,
           CAST(NOW() AS TIMESTAMP)
       ) AS moved_at
FROM final_stage fs;

UPDATE posting_stage s
SET moved_at = b.moved_at
FROM tmp_current_stage_backfill b
WHERE s.id = b.stage_id;

UPDATE posting p
SET current_stage_id = b.stage_id
FROM tmp_current_stage_backfill b
WHERE p.id = b.posting_id
  AND p.current_stage_id IS NULL;

DROP TABLE tmp_current_stage_backfill;
