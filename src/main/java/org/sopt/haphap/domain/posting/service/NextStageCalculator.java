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

    // 현재 진행 전형: 15 이상 중 마지막. 없으면 첫 전형
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