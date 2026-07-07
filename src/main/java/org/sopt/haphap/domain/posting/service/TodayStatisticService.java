package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.TodayStatisticResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayStatisticService {

    private final RegistrationRepository registrationRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final NextStageCalculator nextStageCalculator;

    public TodayStatisticResponse getTodayStatistics() {
        long cumulated = cumulatedCount();
        long onGoing = onGoingCount();
        long announced = announcedCount();
        return new TodayStatisticResponse(cumulated, onGoing, announced);
    }

    // 1. 오늘 등록/변경된 결과 수 (오늘 updatedAt인 Registration)
    private long cumulatedCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfDay.plusDays(1);
        return registrationRepository.countTodayUpdated(startOfDay, startOfTomorrow);
    }

    // 2. 진행 중(마감 안 된) 공고 수 = nextStage가 null이 아닌 공고
    private long onGoingCount() {

        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findAllStages().stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(
                        Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));

        Map<Long, Map<Long, Long>> countsByPosting = stageResultCountRepository
                .findAllTotals().stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        return stagesByPosting.entrySet().stream()
                .filter(entry -> {
                    List<PostingStageFlatProjection> stages = entry.getValue();
                    Map<Long, Long> counts = countsByPosting.getOrDefault(entry.getKey(), Map.of());
                    return !nextStageCalculator.isClosed(stages, counts);  // 마감 안 됨 = 진행 중
                })
                .count();
    }

    // 3. 오늘 발표 감지된 전형이 있는 공고 수
    private long announcedCount() {
        return postingStageRepository.countPostingsAnnouncedToday(LocalDate.now());
    }
}
