package org.sopt.haphap.domain.search.indexing;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.document.PostingSearchDocument;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingSearchIndexer {

    private final ElasticsearchOperations operations;
    private final PostingRepository postingRepository;

    /* 공고 1건 색인(없으면 색인에서 제거) */
    @Transactional(readOnly = true)
    public void index(Long postingId) {
        postingRepository.findWithCompanyAndCategory(postingId).ifPresentOrElse(
                posting -> saveIgnoringStaleWrite(PostingSearchDocument.of(posting)),
                () -> operations.delete(String.valueOf(postingId), PostingSearchDocument.class));
    }

    private void saveIgnoringStaleWrite(PostingSearchDocument doc) {
        try {
            operations.save(doc);
        } catch (DataAccessException e) {
            if (isVersionConflict(e)) {
                log.debug("[검색] 더 최신 문서가 이미 색인되어 있어 건너뜀 postingId={}", doc.getPostingId());
                return;
            }
            throw e;
        }
    }

    private boolean isVersionConflict(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains("version_conflict_engine_exception")) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public int reindexAll() {
        String alias = "posting_search";
        String newIndex = alias + "_v" + System.currentTimeMillis();

        IndexOperations newOps = operations.indexOps(IndexCoordinates.of(newIndex));
        newOps.create(Map.of("number_of_shards", "1", "number_of_replicas", "0"),
                operations.indexOps(PostingSearchDocument.class).createMapping());

        List<PostingSearchDocument> docs = postingRepository.findAllWithCompanyAndCategory(null)
                .stream()
                .map(PostingSearchDocument::of)
                .toList();
        if (!docs.isEmpty()) {
            operations.save(docs, IndexCoordinates.of(newIndex));
        }

        swapAlias(alias, newIndex);

        log.info("[검색] 재색인 완료 - newIndex={}, count={}", newIndex, docs.size());
        return docs.size();
    }

    private void swapAlias(String alias, String newIndex) {
        Set<String> oldIndices = new HashSet<>();
        try {
            Map<String, Set<AliasData>> current = operations
                    .indexOps(IndexCoordinates.of(alias))
                    .getAliasesForIndex(alias);
            oldIndices.addAll(current.keySet());
        } catch (Exception e) {
            log.info("[검색] 기존 alias 없음 - 신규 생성");
        }

        AliasActions actions = new AliasActions();
        for (String oldIndex : oldIndices) {
            actions.add(new AliasAction.Remove(
                    AliasActionParameters.builder()
                            .withIndices(oldIndex)
                            .withAliases(alias)
                            .build()));
        }
        actions.add(new AliasAction.Add(
                AliasActionParameters.builder()
                        .withIndices(newIndex)
                        .withAliases(alias)
                        .build()));

        operations.indexOps(IndexCoordinates.of(newIndex)).alias(actions);

        for (String oldIndex : oldIndices) {
            operations.indexOps(IndexCoordinates.of(oldIndex)).delete();
            log.info("[검색] 이전 인덱스 삭제 - {}", oldIndex);
        }
    }
}