package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.TodayStatisticResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.service.RegistrationQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayStatisticService {

    private final RegistrationQueryService registrationQueryService;
    private final PostingStageRepository postingStageRepository;
    private final PostingRepository postingRepository;

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
        Long count = registrationQueryService.countTodayEvents(startOfDay, startOfTomorrow);
        return count == null ? 0L : count;
    }

    // 2. 진행 중(마감 안 된) 공고 수 = 전형이 1개 이상 있는 공고 중 Posting.isClosed()가 false인 것
    private long onGoingCount() {
        List<Long> postingIdsWithStages = postingStageRepository.findDistinctPostingIds();
        if (postingIdsWithStages.isEmpty()) {
            return 0L;
        }
        return postingRepository.findAllWithCurrentStageByIds(postingIdsWithStages).stream()
                .filter(posting -> !posting.isClosed())
                .count();
    }

    // 3. 오늘 발표 감지된 전형이 있는 공고 수
    private long announcedCount() {
        return postingStageRepository.countPostingsAnnouncedToday(LocalDate.now());
    }
}
