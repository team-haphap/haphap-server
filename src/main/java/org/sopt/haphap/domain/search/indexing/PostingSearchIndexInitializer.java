package org.sopt.haphap.domain.search.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations operations;
    private final PostingSearchIndexer indexer;

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations indexOps = operations.indexOps(PostingSearchDocument.class);
            boolean empty = !indexOps.exists()
                    || operations.count(
                    NativeQuery.builder().withQuery(q -> q.matchAll(m -> m)).build(),
                    PostingSearchDocument.class) == 0;
            if (empty) {
                int count = indexer.reindexAll();
                log.info("[검색] 초기 색인 완료 count={}", count);
            }
        } catch (Exception e) {
            log.warn("[검색] 초기 색인 실패 - Elasticsearch 연결을 확인하세요", e);
        }
    }
}