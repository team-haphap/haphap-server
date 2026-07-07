package org.sopt.haphap.domain.posting.service.aggregate;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageResultAggProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StageResultCountRebuilder {

    private final RegistrationRepository registrationRepository;
    private final StageResultCountRepository stageResultCountRepository;

    /** 원본(Registration)에서 모두 재집계해 StageResultCount를 재구성.. */
    @Transactional
    public void rebuildAll() {
        stageResultCountRepository.deleteAllInBatch();

        // (postingId, stageId) -> StageResultCount 누적
        Map<String, StageResultCount> map = new HashMap<>();

        for (StageResultAggProjection agg : registrationRepository.aggregateAllForRebuild()) {
            String key = agg.getPostingId() + ":" + agg.getStageId();
            StageResultCount row = map.computeIfAbsent(key,
                    k -> StageResultCount.empty(agg.getPostingId(), agg.getStageId()));
            row.add(agg.getResult(), agg.getCnt());
        }

        stageResultCountRepository.saveAll(map.values());
    }
}