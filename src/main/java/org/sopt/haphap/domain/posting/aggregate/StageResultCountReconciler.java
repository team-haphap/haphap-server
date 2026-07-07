package org.sopt.haphap.domain.posting.aggregate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageResultAggProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
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
    @Transactional
    public void reconcile() {
        // 1. 원본에서 실제 카운트 집계 → (postingId:stageId) -> {pass, fail, pending}
        Map<String, long[]> actual = new HashMap<>();  // [pass, fail, pending]
        for (StageResultAggProjection agg : registrationRepository.aggregateAllForRebuild()) {
            String key = agg.getPostingId() + ":" + agg.getStageId();
            long[] arr = actual.computeIfAbsent(key, k -> new long[3]);
            switch (agg.getResult()) {
                case PASS -> arr[0] += agg.getCnt();
                case FAIL -> arr[1] += agg.getCnt();
                case PENDING -> arr[2] += agg.getCnt();
            }
        }

        // 2. 현재 집계 테이블과 대조하며 어긋난 것만 보정
        int fixed = 0;
        List<StageResultCount> existing = stageResultCountRepository.findAll();
        Set<String> seen = new HashSet<>();

        for (StageResultCount row : existing) {
            String key = row.getPostingId() + ":" + row.getStageId();
            seen.add(key);
            long[] a = actual.getOrDefault(key, new long[3]);

            if (row.getPassCount() != a[0] || row.getFailCount() != a[1]
                    || row.getPendingCount() != a[2]) {
                log.warn("집계 불일치 보정 posting={} stage={} | 저장(P{},F{},Pn{}) → 실제(P{},F{},Pn{})",
                        row.getPostingId(), row.getStageId(),
                        row.getPassCount(), row.getFailCount(), row.getPendingCount(),
                        a[0], a[1], a[2]);
                row.reconcile(a[0], a[1], a[2]);  // 보정 메서드
                fixed++;
            }
        }

        // 3. 원본엔 있는데 집계 테이블엔 없는 (posting,stage) → 새로 생성
        int created = 0;
        for (Map.Entry<String, long[]> entry : actual.entrySet()) {
            if (seen.contains(entry.getKey())) continue;
            String[] ids = entry.getKey().split(":");
            long[] a = entry.getValue();
            StageResultCount row = StageResultCount.empty(
                    Long.parseLong(ids[0]), Long.parseLong(ids[1]));
            row.reconcile(a[0], a[1], a[2]);
            stageResultCountRepository.save(row);
            created++;
        }

        log.info("집계 정합성 배치 완료: 보정 {}건, 신규 {}건", fixed, created);
    }
}