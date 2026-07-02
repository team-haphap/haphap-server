package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.springframework.stereotype.Component;

@Component
public class NextStageCalculator {

    private static final int PROGRESS_THRESHOLD = 5;

    /**
     * stages: 한 공고의 전형들 (orderIndex 오름차순 정렬된 상태로 전달)
     * countByStageId: 전형 id -> 등록 수
     * 반환: nextStage (없으면 null)
     */
    public PostingStage calculate(List<PostingStage> stages, Map<Long, Long> countByStageId) {
        if (stages.isEmpty()) {
            return null;
        }

        // 5개 이상 쌓인 전형 중 orderIndex가 가장 큰 것의 "인덱스"를 찾는다.
        int lastProgressedIdx = -1;
        for (int i = 0; i < stages.size(); i++) {
            long cnt = countByStageId.getOrDefault(stages.get(i).getId(), 0L);
            if (cnt >= PROGRESS_THRESHOLD) {
                lastProgressedIdx = i;
            }
        }

        // 아무 전형도 5개 못 채움 → 첫 전형이 nextStage
        if (lastProgressedIdx == -1) {
            return stages.get(0);
        }

        // 진행된 마지막 전형의 다음
        int nextIdx = lastProgressedIdx + 1;

        // 마지막 전형까지 다 찼으면 다음이 없음 → null
        if (nextIdx >= stages.size()) {
            return null;
        }
        return stages.get(nextIdx);
    }

    public Integer daysUntil(PostingStage nextStage) {
        if (nextStage == null || nextStage.getExpectedAnnouncementDate() == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), nextStage.getExpectedAnnouncementDate());
        return (int) days;
    }
}