package org.sopt.haphap.domain.posting.service.aggregate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageResultAggProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class StageResultCountReconciler {

    private final RegistrationRepository registrationRepository;
    private final StageResultCountRepository stageResultCountRepository;

    // 매일 새벽 4시 — 이벤트 유실로 인한 드리프트 보정
    @Scheduled(cron = "0 0 4 * * *")
    public void reconcile() {
        try {
            doReconcile();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("집계 보정 중 낙관적 락 충돌 발생. 다음 배치에서 재보정합니다.", e);
        } catch (DataIntegrityViolationException e) {
            log.warn("집계 보정 중 유니크 충돌(동시 생성). 다음 배치에서 재보정합니다.", e);
        }
    }


    @Transactional
    public void doReconcile() {

        // 1. 원본 집계 스냅샷
        Map<String, long[]> actual = aggregateActual();

        // 2. 기존 row 대조 보정
        List<StageResultCount> existing = stageResultCountRepository.findAll();
        Set<String> seen = new HashSet<>();
        int fixed = 0;

        for (StageResultCount row : existing) {
            String key = key(row.getPostingId(), row.getStageId());
            seen.add(key);
            long[] a = actual.getOrDefault(key, new long[3]);

            if (isDifferent(row, a)) {
                log.warn("집계 불일치 보정 posting={} stage={} | 저장(P{},F{},Pn{}) → 실제(P{},F{},Pn{})",
                        row.getPostingId(), row.getStageId(),
                        row.getPassCount(), row.getFailCount(), row.getPendingCount(),
                        a[0], a[1], a[2]);
                row.reconcile(a[0], a[1], a[2]);  // 더티체킹 UPDATE, version 자동 검사·증가
                fixed++;
            }
        }
        // 3. 누락된 (posting,stage) 신규 생성
        int created = 0;
        for (Map.Entry<String, long[]> entry : actual.entrySet()) {
            if (seen.contains(entry.getKey())) continue;
            long[] ids = parseKey(entry.getKey());
            long[] a = entry.getValue();
            StageResultCount row = StageResultCount.empty(ids[0], ids[1]);
            row.reconcile(a[0], a[1], a[2]);
            stageResultCountRepository.save(row);
            created++;
        }

        log.info("집계 정합성 배치 완료: 보정 {}건, 신규 {}건", fixed, created);
    }
    private Map<String, long[]> aggregateActual() {
        Map<String, long[]> actual = new HashMap<>();
        for (StageResultAggProjection agg : registrationRepository.aggregateAllForRebuild()) {
            String key = key(agg.getPostingId(), agg.getStageId());
            long[] arr = actual.computeIfAbsent(key, k -> new long[3]);
            switch (agg.getResult()) {
                case PASS -> arr[0] += agg.getCnt();
                case FAIL -> arr[1] += agg.getCnt();
                case PENDING -> arr[2] += agg.getCnt();
            }
        }
        return actual;
    }
    private boolean isDifferent(StageResultCount row, long[] a) {
        return row.getPassCount() != a[0]
                || row.getFailCount() != a[1]
                || row.getPendingCount() != a[2];
    }

    private String key(Long postingId, Long stageId) {
        return postingId + ":" + stageId;
    }

    private long[] parseKey(String key) {
        String[] p = key.split(":");
        return new long[]{Long.parseLong(p[0]), Long.parseLong(p[1])};
    }
}