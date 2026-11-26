package org.sopt.haphap.domain.search.keyword;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordLogCleaner {

    private static final int RETENTION_DAYS = 7;

    private final SearchKeywordLogRepository repository;

    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void clean() {
        int deleted = repository.deleteOlderThan(LocalDateTime.now().minusDays(RETENTION_DAYS));
        log.info("[인기 검색어] 오래된 검색 로그 삭제 count={}", deleted);
    }
}