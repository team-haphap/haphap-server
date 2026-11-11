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

    // 공고 저장 트랜잭션이 커밋된 뒤 색인
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PostingSearchIndexEvent event) {
        try {
            indexer.index(event.postingId());
        } catch (Exception e) {
            log.warn("[검색] 공고 색인 실패 postingId={} - 재색인 필요", event.postingId(), e);
        }
    }
}