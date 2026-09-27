package org.sopt.haphap.domain.posting.service.aggregate;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountCleanupScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingStageRepository postingStageRepository;
    private final PostingRepository postingRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void removeClosedPostings() {
        List<Long> postingIdsWithStages = postingStageRepository.findDistinctPostingIds();
        if (postingIdsWithStages.isEmpty()) {
            log.info("전형 있는 공고 없음");
            return;
        }

        List<String> closedIds = postingRepository.findAllWithCurrentStageByIds(postingIdsWithStages).stream()
                .filter(Posting::isClosed)
                .map(posting -> String.valueOf(posting.getId()))
                .toList();

        if (closedIds.isEmpty()) {
            log.info("마감 공고 없음");
            return;
        }
        redisTemplate.opsForZSet().remove(PostingViewTracker.VIEW_COUNT_KEY, closedIds.toArray());
        log.info("마감 공고 {}건 제거: {}", closedIds.size(), closedIds);
    }
}
