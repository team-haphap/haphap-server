package org.sopt.haphap.domain.search.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchIndexListener {

    private final PostingSearchIndexer indexer;
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 1_000;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PostingSearchIndexEvent event) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                indexer.index(event.postingId());
                return;
            } catch (Exception e) {
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("[검색] 공고 색인 {}회 실패 postingId={} - 일일 재색인에서 복구",
                            MAX_ATTEMPTS, event.postingId(), e);
                    return;
                }
                sleep(RETRY_BACKOFF_MS * attempt);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}