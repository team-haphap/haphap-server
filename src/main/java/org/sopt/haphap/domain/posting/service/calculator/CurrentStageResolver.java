package org.sopt.haphap.domain.posting.service.calculator;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 현재 진행 전형 판정을 담당 (재사용)
@Component
@RequiredArgsConstructor
public class CurrentStageResolver {

    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final NextStageCalculator nextStageCalculator;

    // 현재 진행 여부
    public String resolveCurrentState(Long postingId) {
        List<PostingStageFlatProjection> stages =
                postingStageRepository.findFlatByPostingIds(List.of(postingId));
        stages.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex));

        Map<Long, Long> counts = stageResultCountRepository
                .findTotalsByPostingIds(List.of(postingId)).stream()
                .collect(Collectors.toMap(
                        StageRegistrationCountProjection::getStageId,
                        StageRegistrationCountProjection::getCnt));

        if (nextStageCalculator.isClosed(stages, counts)) {
            return "마감";
        }

        PostingStageFlatProjection current = nextStageCalculator.currentStage(stages, counts);
        if (current == null) {
            return "진행 예정";                    // 첫 전형이 아직 5 미달
        }
        return current.getName() + " 진행 중";
    }
    public String resolveCurrentStageName(Long postingId) {
        List<PostingStageFlatProjection> stages =
                postingStageRepository.findFlatByPostingIds(List.of(postingId));
        stages.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex));

        Map<Long, Long> counts = stageResultCountRepository
                .findTotalsByPostingIds(List.of(postingId)).stream()
                .collect(Collectors.toMap(
                        StageRegistrationCountProjection::getStageId,
                        StageRegistrationCountProjection::getCnt));

        if (nextStageCalculator.isClosed(stages, counts)) return null;   // 마감 → 알람 없음

        PostingStageFlatProjection current = nextStageCalculator.currentStage(stages, counts);
        return current == null ? null : current.getName();
    }
}