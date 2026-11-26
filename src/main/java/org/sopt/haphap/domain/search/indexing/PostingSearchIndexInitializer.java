package org.sopt.haphap.domain.search.indexing;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
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
    private void ensureValidIndex() {
        String alias = "posting_search";

        boolean aliasExists = isAliasExists(alias);

        if (!aliasExists) {
            String initialIndex = alias + "_v" + System.currentTimeMillis();
            IndexOperations initOps = operations.indexOps(IndexCoordinates.of(initialIndex));
            initOps.create(
                    Map.of("number_of_shards", "1", "number_of_replicas", "0"),
                    operations.indexOps(PostingSearchDocument.class).createMapping()
            );
            initOps.alias(new AliasActions().add(
                    new AliasAction.Add(AliasActionParameters.builder()
                            .withIndices(initialIndex)
                            .withAliases(alias)
                            .build())));
            log.info("[검색] 초기 인덱스+alias 생성 완료 - index={}", initialIndex);
        } else if (isMappingCorrupted(operations.indexOps(PostingSearchDocument.class))) {
            log.warn("[검색] 매핑 오염 감지 - 재색인 시작");
            indexer.reindexAll();
        } else {
            log.info("[검색] 인덱스 alias 정상 확인");
        }
    }

    private boolean isAliasExists(String alias) {
        try {
            Map<String, Set<AliasData>> result = operations
                    .indexOps(IndexCoordinates.of(alias))
                    .getAliasesForIndex(alias);
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            return false;
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