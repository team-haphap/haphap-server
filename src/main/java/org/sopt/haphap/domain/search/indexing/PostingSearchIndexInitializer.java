package org.sopt.haphap.domain.search.indexing;

import java.util.Map;
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
            ensureValidIndex();

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

    private void ensureValidIndex() {
        IndexOperations indexOps = operations.indexOps(PostingSearchDocument.class);
        if (indexOps.exists()) {
            if (isMappingCorrupted(indexOps)) {
                log.warn("[검색] 동적 매핑 오염 감지(companyText) - 인덱스 재생성 진행");
                indexOps.delete();
                indexOps.createWithMapping();
            }
        } else {
            indexOps.createWithMapping();
            log.info("[검색] 신규 인덱스 및 매핑 생성 완료");
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isMappingCorrupted(IndexOperations indexOps) {
        Map<String, Object> mapping = indexOps.getMapping();
        if (mapping == null || mapping.isEmpty()) {
            return true;
        }
        Object propertiesObj = mapping.get("properties");
        if (!(propertiesObj instanceof Map<?, ?> properties)) {
            return true;
        }
        Object companyTextObj = properties.get("companyText");
        if (!(companyTextObj instanceof Map<?, ?> companyTextField)) {
            return true;
        }
        // 동적 매핑에 의해 companyText가 text 타입으로 생성된 경우 오염으로 판단
        Object type = companyTextField.get("type");
        return "text".equals(type);
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