package org.sopt.haphap.domain.posting.service.calculator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NextStageCalculator {

    private static final int PROGRESS_THRESHOLD = 5;
    private static final int CLOSE_GRACE_DAYS = 2;

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

    // 마감: 마지막 전형에 도달(=calculate()가 null)한 뒤, 그 전형 이동일(announcedDate)로부터 2일 지난 시점부터
    public boolean isClosed(List<PostingStageFlatProjection> stages, Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) return false;
        if (calculate(stages, countByStageId) != null) return false;   // 다음 전형이 남아있으면 진행 중

        // calculate()==null 이면 lastProgressedIndex는 항상 마지막 인덱스 → 마지막 전형이 곧 이 stage
        PostingStageFlatProjection lastStage = stages.get(stages.size() - 1);
        LocalDate movedDate = lastStage.getAnnouncedDate();
        if (movedDate == null) return false;   // 이동일 미기록 → 아직 마감시키지 않음(안전하게 유예)

        return !LocalDate.now().isBefore(movedDate.plusDays(CLOSE_GRACE_DAYS));
    }
}