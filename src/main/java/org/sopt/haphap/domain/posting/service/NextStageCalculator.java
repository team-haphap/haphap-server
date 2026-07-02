package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.dto.PostingStageFlatProjection;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NextStageCalculator {

    private static final int PROGRESS_THRESHOLD = 15;

    /**
     * stages: 한 공고의 전형들 (orderIndex 오름차순 정렬된 상태로 전달)
     * countByStageId: 전형 id -> 등록 수
     * 반환: nextStage (없으면 null)
     */
    /*
    public PostingStageFlatProjection calculate(List<PostingStageFlatProjection> stages, Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) {
            return null;
        }

        // 15개 이상 쌓인 전형 중 orderIndex가 가장 큰 것의 "인덱스"를 찾는다.
        int lastProgressedIdx = -1;
        for (int i = 0; i < stages.size(); i++) {
            long cnt = countByStageId.getOrDefault(stages.get(i).getStageId(), 0L);
            log.info("stageId={}, name={}, cnt={}",
                    stages.get(i).getStageId(), stages.get(i).getName(), cnt);
            if (cnt >= PROGRESS_THRESHOLD) {
                lastProgressedIdx = i;
            }
        }
        log.info(">>> lastProgressedIdx={}, nextStage={}",
                lastProgressedIdx,
                (lastProgressedIdx + 1 < stages.size()) ? stages.get(lastProgressedIdx + 1).getName() : "null");

        // 아무 전형도 15개 못 채움 → 첫 전형이 nextStage
        if (lastProgressedIdx == -1) return stages.get(0);

        // 진행된 마지막 전형의 다음
        int nextIdx = lastProgressedIdx + 1;

        // 마지막 전형까지 다 찼으면 다음이 없음 → null
        if (nextIdx >= stages.size()) {
            return null;
        }
        return stages.get(nextIdx);
    }

    public Integer daysUntil(PostingStageFlatProjection nextStage) {
        if (nextStage == null || nextStage.getExpectedAnnouncementDate() == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), nextStage.getExpectedAnnouncementDate());
        return (int) days;
    }

     */
    // 15 이상 중 마지막 인덱스 (없으면 -1)
    private int lastProgressedIndex(List<PostingStageFlatProjection> stages,
                                    Map<Long, Long> countByStageId) {
        int idx = -1;
        for (int i = 0; i < stages.size(); i++) {
            long cnt = countByStageId.getOrDefault(stages.get(i).getStageId(), 0L);
            if (cnt >= PROGRESS_THRESHOLD) idx = i;
        }
        return idx;
    }

    // 현재 진행 전형: 15 이상 중 마지막. 없으면 첫 전형(서류 받는 중).
    public PostingStageFlatProjection currentStage(List<PostingStageFlatProjection> stages,
                                                   Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) return null;
        int last = lastProgressedIndex(stages, countByStageId);
        return (last == -1) ? stages.get(0) : stages.get(last);
    }

    // 다음 전형: 15 이상 중 마지막의 다음. 아무것도 없으면 첫 전형. 다 찼으면 null.
    public PostingStageFlatProjection calculate(List<PostingStageFlatProjection> stages,
                                                Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) return null;
        int last = lastProgressedIndex(stages, countByStageId);
        if (last == -1) return stages.get(0);
        int nextIdx = last + 1;
        return (nextIdx >= stages.size()) ? null : stages.get(nextIdx);
    }

    public Integer daysUntil(PostingStageFlatProjection nextStage) {
        if (nextStage == null || nextStage.getExpectedAnnouncementDate() == null) return null;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), nextStage.getExpectedAnnouncementDate());
    }
}