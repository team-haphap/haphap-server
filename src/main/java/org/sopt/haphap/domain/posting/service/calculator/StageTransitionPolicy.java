package org.sopt.haphap.domain.posting.service.calculator;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.sopt.haphap.domain.posting.domain.StageType;
import org.springframework.stereotype.Component;

// 전형 이동 최소 시차표기준. 현재 전형에서 다음 전형으로 넘어가기까지 최소로 기다려야 하는 시간.
@Component
public class StageTransitionPolicy {

    private static final Map<StageType, Duration> MIN_GAP_FROM = new EnumMap<>(StageType.class);

    static {
        MIN_GAP_FROM.put(StageType.DOCUMENT, Duration.ofHours(24));
        MIN_GAP_FROM.put(StageType.APTITUDE_OR_CODING_TEST, Duration.ofHours(48));
        MIN_GAP_FROM.put(StageType.FIRST_INTERVIEW, Duration.ofHours(72));
        MIN_GAP_FROM.put(StageType.SECOND_INTERVIEW, Duration.ofHours(72));
        MIN_GAP_FROM.put(StageType.EXECUTIVE_INTERVIEW, Duration.ofHours(24));
    }

    // currentStageType 이후로는 이동이 없으므로(FINAL_PASS) 정의되지 않은 전이로 호출하면 오류.
    public Duration minGapFrom(StageType currentStageType) {
        Duration gap = MIN_GAP_FROM.get(currentStageType);
        if (gap == null) {
            throw new IllegalStateException("정의되지 않은 전형 전이입니다: " + currentStageType);
        }
        return gap;
    }
}
