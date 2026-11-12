package org.sopt.haphap.domain.search.indexing;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchIndexInitializer {

    private final ElasticsearchOperations operations;
    private final PostingSearchIndexer indexer;
    private final PostingRepository postingRepository;

    private final AtomicBoolean synced = new AtomicBoolean(false);

    @Scheduled(initialDelay = 5_000, fixedDelay = 30_000)
    public void ensureIndexSynced() {
        if (synced.get()) {
            return;
        }
        try {
            long dbCount = postingRepository.count();
            long esCount = countIndexed();
            if (esCount != dbCount) {
                int count = indexer.reindexAll();
                log.info("[검색] 초기 색인 동기화 완료 db={} es={} -> {}", dbCount, esCount, count);
            }
            synced.set(true);
        } catch (Exception e) {
            log.warn("[검색] 초기 색인 동기화 실패 - 30초 후 재시도", e);
        }
    }

    private long countIndexed() {
        IndexOperations indexOps = operations.indexOps(PostingSearchDocument.class);
        if (!indexOps.exists()) {
            return -1;
        }
        return operations.count(
                NativeQuery.builder().withQuery(q -> q.matchAll(m -> m)).build(),
                PostingSearchDocument.class);
    }
}