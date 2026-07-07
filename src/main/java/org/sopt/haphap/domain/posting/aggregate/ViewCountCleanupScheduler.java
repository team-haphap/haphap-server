package org.sopt.haphap.domain.posting.aggregate;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountCleanupScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingRepository postingRepository;

    // 매시 정각: 인기 공고 집계용 카운터 전체 0으로 리셋
    @Scheduled(cron = "0 0 * * * *")
    public void resetHourlyViewCounts() {
        redisTemplate.delete(PostingViewTracker.VIEW_COUNT_KEY);
    }

    // 매일 자정(24시): 마감된 공고를 집계 대상에서 제거
    @Scheduled(cron = "0 0 0 * * *")
    public void removeClosedPostings() {
        List<Long> closedIds = postingRepository.findClosedPostingIds();
        if (closedIds.isEmpty()) {
            return;
        }
        String[] members = closedIds.stream().map(String::valueOf).toArray(String[]::new);
        redisTemplate.opsForZSet().remove(PostingViewTracker.VIEW_COUNT_KEY, (Object[]) members);
    }
}