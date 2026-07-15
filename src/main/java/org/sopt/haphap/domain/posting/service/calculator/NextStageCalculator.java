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

    private int lastProgressedIndex(List<PostingStageFlatProjection> stages,
                                    Map<Long, Long> countByStageId) {
        int idx = -1;
        for (int i = 0; i < stages.size(); i++) {
            long cnt = countByStageId.getOrDefault(stages.get(i).getStageId(), 0L);
            if (cnt >= PROGRESS_THRESHOLD) idx = i;
        }
        return idx;
    }

    public PostingStageFlatProjection currentStage(
            List<PostingStageFlatProjection> stages, Map<Long, Long> counts) {
        PostingStageFlatProjection lastProgressed = null;
        for (PostingStageFlatProjection stage : stages) {   // orderIndex 오름차순 전제
            long count = counts.getOrDefault(stage.getStageId(), 0L);
            if (count >= PROGRESS_THRESHOLD) {
                lastProgressed = stage;
            } else {
                break;      // 연속 조건 (건너뛰기 방지)
            }
        }
        return lastProgressed;   // ← null이면 "아직 발표 시작된 전형 없음". fallback 없음
    }

    public PostingStageFlatProjection calculate(List<PostingStageFlatProjection> stages,
                                                Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) return null;
        int last = lastProgressedIndex(stages, countByStageId);
        if (last == -1) return stages.get(0);
        int nextIdx = last + 1;
        return (nextIdx >= stages.size()) ? null : stages.get(nextIdx);
    }

    // 마감: 마지막 전형에 도달(=calculate()가 null)한 뒤, 그 전형 이동일(announcedDate)로부터 2일 지난 시점부터
    public boolean isClosed(List<PostingStageFlatProjection> stages, Map<Long, Long> counts) {
        if (stages.isEmpty()) return false;
        if (calculate(stages, counts) != null) return false;   // 다음 전형이 남아있으면 진행 중

        PostingStageFlatProjection lastStage = stages.get(stages.size() - 1);
        LocalDate movedDate = lastStage.getAnnouncedDate();
        if (movedDate == null) return false;   // 이동일 미기록 → 아직 마감시키지 않음(안전하게 유예)

        return !LocalDate.now().isBefore(movedDate.plusDays(CLOSE_GRACE_DAYS));
    }

    /** 목록에 보여줄 전형과 라벨 */
    public StageDisplay resolveDisplay(List<PostingStageFlatProjection> stages, Map<Long, Long> counts) {
        if (isClosed(stages, counts)) {
            return new StageDisplay(null, "마감", true);
        }
        PostingStageFlatProjection current = currentStage(stages, counts);
        PostingStageFlatProjection next = calculate(stages, counts);

        if (current != null && LocalDate.now().equals(current.getAnnouncedDate())) {
            return new StageDisplay(current, "D-day", false);
        }
        if (next == null) {
            return new StageDisplay(current, "D-day", false);
        }
        return new StageDisplay(next, toLabel(daysUntil(next)), false);
    }

    public Integer daysUntil(PostingStageFlatProjection nextStage) {
        if (nextStage == null || nextStage.getExpectedAnnouncementDate() == null) return null;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), nextStage.getExpectedAnnouncementDate());
    }

    private String toLabel(Integer days) {
        if (days == null) return null;             // 발표 예정일 미정
        return days <= 0 ? "D-0" : "D-" + days;
    }

    public record StageDisplay(PostingStageFlatProjection stage, String label, boolean closed) {}
}