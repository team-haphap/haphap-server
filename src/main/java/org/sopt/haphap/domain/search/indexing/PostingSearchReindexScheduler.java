package org.sopt.haphap.domain.search.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/* 단건 색인 실패·누락으로 생긴 드리프트를 매일 새벽 전체 재색인으로 정리 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchReindexScheduler {

    private final PostingSearchIndexer indexer;

    @Scheduled(cron = "0 10 4 * * *")   // 검색 로그 정리(04:30)와 겹치지 않게
    public void reindex() {
        try {
            int count = indexer.reindexAll();
            log.info("[검색] 전체 재색인 완료 count={}", count);
        } catch (Exception e) {
            log.error("[검색] 전체 재색인 실패 - 다음 주기에 재시도", e);
        }
    }
}